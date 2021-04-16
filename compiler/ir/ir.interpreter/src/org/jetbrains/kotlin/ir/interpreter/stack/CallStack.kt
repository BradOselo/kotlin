/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.interpreter.stack

import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.interpreter.CompoundInstruction
import org.jetbrains.kotlin.ir.interpreter.Instruction
import org.jetbrains.kotlin.ir.interpreter.SimpleInstruction
import org.jetbrains.kotlin.ir.interpreter.exceptions.InterpreterError
import org.jetbrains.kotlin.ir.interpreter.state.State
import org.jetbrains.kotlin.ir.interpreter.state.StateWithClosure
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.util.fileOrNull
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.utils.addToStdlib.firstNotNullResult

private class DataStack {
    private val stack = mutableListOf<State>()

    fun isEmpty() = stack.isEmpty()

    fun push(state: State) {
        stack.add(state)
    }

    fun pop(): State = stack.removeLast()
    fun peek(): State = stack.last()
}

internal class CallStack {
    private val frames = mutableListOf<CallStackFrameContainer>()
    private fun getCurrentFrame() = frames.last()
    internal fun getCurrentFrameOwner() = frames.last().currentSubFrameOwner

    fun newFrame(frameOwner: IrElement, instructions: List<Instruction>, irFile: IrFile? = null) {
        val newFrame = SubFrame(instructions.toMutableList(), frameOwner)
        frames.add(CallStackFrameContainer(newFrame, irFile))
    }

    fun newFrame(frameOwner: IrFunction, instructions: List<Instruction>) {
        val newFrame = SubFrame(instructions.toMutableList(), frameOwner)
        frames.add(CallStackFrameContainer(newFrame, frameOwner.fileOrNull))
    }

    fun newSubFrame(frameOwner: IrElement, instructions: List<Instruction>) {
        val newFrame = SubFrame(instructions.toMutableList(), frameOwner)
        getCurrentFrame().addSubFrame(newFrame)
    }

    fun dropFrame() {
        frames.removeLast()
    }

    fun dropFrameAndCopyResult() {
        val result = peekState() ?: return dropFrame()
        popState()
        dropFrame()
        pushState(result)
    }

    fun dropSubFrame() {
        getCurrentFrame().removeSubFrame()
    }

    fun returnFromFrameWithResult(irReturn: IrReturn) {
        val result = popState()
        var frameOwner = getCurrentFrameOwner()
        while (frameOwner != irReturn.returnTargetSymbol.owner) {
            when (frameOwner) {
                is IrTry -> {
                    dropSubFrame()
                    pushState(result)
                    addInstruction(SimpleInstruction(irReturn))
                    addInstruction(CompoundInstruction(frameOwner.finallyExpression))
                    return
                }
                is IrCatch -> {
                    val tryBlock = getCurrentFrame().dropInstructions()!!.element as IrTry// last instruction in `catch` block is `try`
                    dropSubFrame()
                    pushState(result)
                    addInstruction(SimpleInstruction(irReturn))
                    addInstruction(CompoundInstruction(tryBlock.finallyExpression))
                    return
                }
                else -> {
                    dropSubFrame()
                    if (getCurrentFrame().hasNoFrames() && frameOwner != irReturn.returnTargetSymbol.owner) dropFrame()
                    frameOwner = getCurrentFrameOwner()
                }
            }
        }

        dropFrame()
        // check that last frame is not a function itself; use case for proxyInterpret
        if (frames.size == 0) newFrame(irReturn, emptyList()) // just stub frame
        pushState(result)
    }

    fun unrollInstructionsForBreakContinue(breakOrContinue: IrBreakContinue) {
        var frameOwner = getCurrentFrameOwner()
        while (frameOwner != breakOrContinue.loop) {
            when (frameOwner) {
                is IrTry -> {
                    addInstruction(CompoundInstruction(breakOrContinue))
                    addInstruction(SimpleInstruction(frameOwner))
                    return
                }
                is IrCatch -> {
                    val tryInstruction = getCurrentFrame().dropInstructions()!! // last instruction in `catch` block is `try`
                    addInstruction(CompoundInstruction(breakOrContinue))
                    addInstruction(tryInstruction)
                    return
                }
                else -> {
                    getCurrentFrame().removeSubFrameWithoutDataPropagation()
                    frameOwner = getCurrentFrameOwner()
                }
            }
        }

        when (breakOrContinue) {
            is IrBreak -> getCurrentFrame().removeSubFrameWithoutDataPropagation() // drop loop
            else -> addInstruction(CompoundInstruction(breakOrContinue.loop))
        }
    }

    fun dropFrameUntilTryCatch() {
        val exception = popState()
        var frameOwner = getCurrentFrameOwner()
        while (frames.isNotEmpty()) {
            val frame = getCurrentFrame()
            while (!frame.hasNoFrames()) {
                frameOwner = frame.currentSubFrameOwner
                when (frameOwner) {
                    is IrTry -> {
                        dropSubFrame()  // drop all instructions that left
                        newSubFrame(frameOwner, listOf())
                        addInstruction(SimpleInstruction(frameOwner)) // to evaluate finally at the end
                        frameOwner.catches.reversed().forEach { addInstruction(CompoundInstruction(it)) }
                        pushState(exception)
                        return
                    }
                    is IrCatch -> {
                        // in case of exception in catch, drop everything except of last `try` instruction
                        addInstruction(frame.dropInstructions()!!)
                        pushState(exception)
                        return
                    }
                    else -> frame.removeSubFrameWithoutDataPropagation()
                }
            }
            dropFrame()
        }

        if (frames.size == 0) newFrame(frameOwner, emptyList()) // just stub frame
        pushState(exception)
    }

    fun hasNoInstructions() = frames.isEmpty() || (frames.size == 1 && frames.first().hasNoInstructions())

    fun addInstruction(instruction: Instruction) {
        getCurrentFrame().addInstruction(instruction)
    }

    fun popInstruction(): Instruction {
        return getCurrentFrame().popInstruction()
    }

    fun pushState(state: State) {
        getCurrentFrame().pushState(state)
    }

    fun popState(): State = getCurrentFrame().popState()
    fun peekState(): State? = getCurrentFrame().peekState()

    fun addVariable(variable: Variable) {
        getCurrentFrame().addVariable(variable)
    }

    fun getVariable(symbol: IrSymbol): Variable = getCurrentFrame().getVariable(symbol)

    fun storeUpValues(state: StateWithClosure) {
        // TODO save only necessary declarations
        state.upValues.addAll(getCurrentFrame().getAll().toMutableList())
    }

    fun loadUpValues(state: StateWithClosure) {
        state.upValues.forEach { addVariable(it) }
    }

    fun copyUpValuesFromPreviousFrame() {
        frames[frames.size - 2].getAll().forEach { addVariable(it) }
    }

    fun getStackTrace(): List<String> {
        return frames.map { it.toString() }.filter { it != CallStackFrameContainer.NOT_DEFINED }
    }

    fun getFileAndPositionInfo(): String {
        return frames[frames.size - 2].getFileAndPositionInfo()
    }

    fun getStackCount(): Int = frames.size
}

private class CallStackFrameContainer(frame: SubFrame, val irFile: IrFile? = null) {
    private val innerStack = mutableListOf(frame)
    private var currentInstruction: Instruction? = null
    val currentSubFrameOwner: IrElement
        get() = getCurrentFrame().owner

    companion object {
        const val NOT_DEFINED = "Not defined"
    }

    private fun getCurrentFrame() = innerStack.last()

    fun addSubFrame(frame: SubFrame) {
        innerStack.add(frame)
    }

    fun removeSubFrame() {
        getCurrentFrame().peekState()?.let { if (innerStack.size > 1) innerStack[innerStack.size - 2].pushState(it) }
        removeSubFrameWithoutDataPropagation()
    }

    fun removeSubFrameWithoutDataPropagation() {
        innerStack.removeLast()
    }

    fun hasNoFrames() = innerStack.isEmpty()
    fun hasNoInstructions() = hasNoFrames() || (innerStack.size == 1 && innerStack.first().isEmpty())

    fun addInstruction(instruction: Instruction) {
        getCurrentFrame().pushInstruction(instruction)
    }

    fun popInstruction(): Instruction {
        return getCurrentFrame().popInstruction().apply { currentInstruction = this }
    }

    fun dropInstructions() = getCurrentFrame().dropInstructions()

    fun pushState(state: State) {
        getCurrentFrame().pushState(state)
    }

    fun popState(): State = getCurrentFrame().popState()
    fun peekState(): State? = getCurrentFrame().peekState()

    fun addVariable(variable: Variable) {
        getCurrentFrame().addVariable(variable)
    }

    fun getVariable(symbol: IrSymbol): Variable {
        return innerStack.firstNotNullResult { it.getVariable(symbol) }
            ?: throw InterpreterError("$symbol not found") // TODO better message
    }

    fun getAll(): List<Variable> = innerStack.flatMap { it.getAll() }

    private fun getLineNumberForCurrentInstruction(): String {
        irFile ?: return ""
        val frameOwner = currentInstruction?.element
        return when {
            frameOwner is IrExpression || (frameOwner is IrDeclaration && frameOwner.origin == IrDeclarationOrigin.DEFINED) ->
                ":${irFile.fileEntry.getLineNumber(frameOwner.startOffset) + 1}"
            else -> ""
        }
    }

    fun getFileAndPositionInfo(): String {
        irFile ?: return NOT_DEFINED
        val lineNum = getLineNumberForCurrentInstruction()
        return "${irFile.name}$lineNum"
    }

    override fun toString(): String {
        irFile ?: return NOT_DEFINED
        val fileNameCapitalized = irFile.name.replace(".kt", "Kt").capitalize()
        val entryPoint = innerStack.firstOrNull { it.owner is IrFunction }?.owner as? IrFunction
        val lineNum = getLineNumberForCurrentInstruction()

        return "at $fileNameCapitalized.${entryPoint?.fqNameWhenAvailable ?: "<clinit>"}(${irFile.name}$lineNum)"
    }
}

private class SubFrame(private val instructions: MutableList<Instruction>, val owner: IrElement) {
    private val memory = mutableListOf<Variable>()
    private val dataStack = DataStack()

    fun isEmpty() = instructions.isEmpty()

    fun pushInstruction(instruction: Instruction) {
        instructions.add(0, instruction)
    }

    fun popInstruction(): Instruction {
        return instructions.removeFirst()
    }

    fun dropInstructions() = instructions.firstOrNull()?.apply { instructions.clear() }

    fun pushState(state: State) {
        dataStack.push(state)
    }

    fun popState(): State = dataStack.pop()
    fun peekState(): State? = if (!dataStack.isEmpty()) dataStack.peek() else null

    fun addVariable(variable: Variable) {
        memory += variable
    }

    fun getVariable(symbol: IrSymbol): Variable? = memory.firstOrNull { it.symbol == symbol }
    fun getAll(): List<Variable> = memory
}