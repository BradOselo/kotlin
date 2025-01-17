/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.serialization

import org.jetbrains.kotlin.backend.common.serialization.proto.IrConstructorCall as ProtoIrConstructorCall
import org.jetbrains.kotlin.backend.common.serialization.proto.IrInlineClassRepresentation as ProtoIrInlineClassRepresentation
import org.jetbrains.kotlin.backend.common.serialization.proto.IrVariable as ProtoIrVariable
import org.jetbrains.kotlin.backend.common.serialization.proto.PirAnonymousInitializerCarrier
import org.jetbrains.kotlin.backend.common.serialization.proto.PirClassCarrier
import org.jetbrains.kotlin.backend.common.serialization.proto.PirConstructorCarrier
import org.jetbrains.kotlin.backend.common.serialization.proto.PirEnumEntryCarrier
import org.jetbrains.kotlin.backend.common.serialization.proto.PirErrorDeclarationCarrier
import org.jetbrains.kotlin.backend.common.serialization.proto.PirFieldCarrier
import org.jetbrains.kotlin.backend.common.serialization.proto.PirFunctionCarrier
import org.jetbrains.kotlin.backend.common.serialization.proto.PirLocalDelegatedPropertyCarrier
import org.jetbrains.kotlin.backend.common.serialization.proto.PirPropertyCarrier
import org.jetbrains.kotlin.backend.common.serialization.proto.PirTypeAliasCarrier
import org.jetbrains.kotlin.backend.common.serialization.proto.PirTypeParameterCarrier
import org.jetbrains.kotlin.backend.common.serialization.proto.PirValueParameterCarrier
import org.jetbrains.kotlin.descriptors.DescriptorVisibility
import org.jetbrains.kotlin.descriptors.InlineClassRepresentation
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.declarations.persistent.carriers.AnonymousInitializerCarrier
import org.jetbrains.kotlin.ir.declarations.persistent.carriers.ClassCarrier
import org.jetbrains.kotlin.ir.declarations.persistent.carriers.ConstructorCarrier
import org.jetbrains.kotlin.ir.declarations.persistent.carriers.EnumEntryCarrier
import org.jetbrains.kotlin.ir.declarations.persistent.carriers.ErrorDeclarationCarrier
import org.jetbrains.kotlin.ir.declarations.persistent.carriers.FieldCarrier
import org.jetbrains.kotlin.ir.declarations.persistent.carriers.FunctionCarrier
import org.jetbrains.kotlin.ir.declarations.persistent.carriers.LocalDelegatedPropertyCarrier
import org.jetbrains.kotlin.ir.declarations.persistent.carriers.PropertyCarrier
import org.jetbrains.kotlin.ir.declarations.persistent.carriers.TypeAliasCarrier
import org.jetbrains.kotlin.ir.declarations.persistent.carriers.TypeParameterCarrier
import org.jetbrains.kotlin.ir.declarations.persistent.carriers.ValueParameterCarrier
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpressionBody
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrFieldSymbol
import org.jetbrains.kotlin.ir.symbols.IrPropertySymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.symbols.IrTypeParameterSymbol
import org.jetbrains.kotlin.ir.symbols.IrValueParameterSymbol
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.IrType

// Auto-generated by compiler/ir/ir.tree.persistent/generator/src/org/jetbrains/kotlin/ir/persistentIrGenerator/Main.kt. DO NOT EDIT!

internal abstract class IrCarrierSerializer {

    abstract fun serializeParentSymbol(value: IrSymbol): Long

    abstract fun serializeOrigin(value: IrDeclarationOrigin): Int

    abstract fun serializeAnnotation(value: IrConstructorCall): ProtoIrConstructorCall

    abstract fun serializeBody(value: IrBody): Int

    abstract fun serializeBlockBody(value: IrBlockBody): Int

    abstract fun serializeExpressionBody(value: IrExpressionBody): Int

    abstract fun serializeValueParameter(value: IrValueParameterSymbol): Long

    abstract fun serializeTypeParameter(value: IrTypeParameterSymbol): Long

    abstract fun serializeSuperType(value: IrType): Int

    abstract fun serializeType(value: IrType): Int

    abstract fun serializeClass(value: IrClassSymbol): Long

    abstract fun serializePropertySymbol(value: IrPropertySymbol): Long

    abstract fun serializeSimpleFunction(value: IrSimpleFunctionSymbol): Long

    abstract fun serializeSimpleFunctionSymbol(value: IrSimpleFunctionSymbol): Long

    abstract fun serializeField(value: IrFieldSymbol): Long

    abstract fun serializeVariable(value: IrVariable): ProtoIrVariable

    abstract fun serializeVisibility(value: DescriptorVisibility): Long

    abstract fun serializeModality(value: Modality): Long

    abstract fun serializeInlineClassRepresentation(value: InlineClassRepresentation<IrSimpleType>): ProtoIrInlineClassRepresentation

    abstract fun serializeIsExternalClass(value: Boolean): Long

    abstract fun serializeIsExternalField(value: Boolean): Long

    abstract fun serializeIsExternalFunction(value: Boolean): Long

    abstract fun serializeIsExternalProperty(value: Boolean): Long

    fun serializeAnonymousInitializerCarrier(carrier: AnonymousInitializerCarrier): ByteArray {
        val proto = PirAnonymousInitializerCarrier.newBuilder()
        proto.setLastModified(carrier.lastModified)
        carrier.parentSymbolField?.let { proto.setParentSymbol(serializeParentSymbol(it)) }
        proto.setOrigin(serializeOrigin(carrier.originField))
        proto.addAllAnnotation(carrier.annotationsField.map { serializeAnnotation(it) })
        carrier.bodyField?.let { proto.setBody(serializeBlockBody(it)) }
        return proto.build().toByteArray()
    }

    fun serializeClassCarrier(carrier: ClassCarrier): ByteArray {
        val proto = PirClassCarrier.newBuilder()
        proto.setLastModified(carrier.lastModified)
        carrier.parentSymbolField?.let { proto.setParentSymbol(serializeParentSymbol(it)) }
        proto.setOrigin(serializeOrigin(carrier.originField))
        proto.addAllAnnotation(carrier.annotationsField.map { serializeAnnotation(it) })
        carrier.thisReceiverSymbolField?.let { proto.setThisReceiver(serializeValueParameter(it)) }
        proto.setFlags(serializeVisibility(carrier.visibilityField) or serializeModality(carrier.modalityField))
        proto.addAllTypeParameters(carrier.typeParametersSymbolField.map { serializeTypeParameter(it) })
        proto.addAllSuperTypes(carrier.superTypesField.map { serializeSuperType(it) })
        carrier.inlineClassRepresentationField?.let { proto.setInlineClassRepresentation(serializeInlineClassRepresentation(it)) }
        return proto.build().toByteArray()
    }

    fun serializeConstructorCarrier(carrier: ConstructorCarrier): ByteArray {
        val proto = PirConstructorCarrier.newBuilder()
        proto.setLastModified(carrier.lastModified)
        carrier.parentSymbolField?.let { proto.setParentSymbol(serializeParentSymbol(it)) }
        proto.setOrigin(serializeOrigin(carrier.originField))
        proto.addAllAnnotation(carrier.annotationsField.map { serializeAnnotation(it) })
        proto.setReturnTypeField(serializeType(carrier.returnTypeFieldField))
        carrier.dispatchReceiverParameterSymbolField?.let { proto.setDispatchReceiverParameter(serializeValueParameter(it)) }
        carrier.extensionReceiverParameterSymbolField?.let { proto.setExtensionReceiverParameter(serializeValueParameter(it)) }
        carrier.bodyField?.let { proto.setBody(serializeBody(it)) }
        proto.setFlags(serializeVisibility(carrier.visibilityField))
        proto.addAllTypeParameters(carrier.typeParametersSymbolField.map { serializeTypeParameter(it) })
        proto.addAllValueParameters(carrier.valueParametersSymbolField.map { serializeValueParameter(it) })
        return proto.build().toByteArray()
    }

    fun serializeEnumEntryCarrier(carrier: EnumEntryCarrier): ByteArray {
        val proto = PirEnumEntryCarrier.newBuilder()
        proto.setLastModified(carrier.lastModified)
        carrier.parentSymbolField?.let { proto.setParentSymbol(serializeParentSymbol(it)) }
        proto.setOrigin(serializeOrigin(carrier.originField))
        proto.addAllAnnotation(carrier.annotationsField.map { serializeAnnotation(it) })
        carrier.correspondingClassSymbolField?.let { proto.setCorrespondingClass(serializeClass(it)) }
        carrier.initializerExpressionField?.let { proto.setInitializerExpression(serializeExpressionBody(it)) }
        return proto.build().toByteArray()
    }

    fun serializeErrorDeclarationCarrier(carrier: ErrorDeclarationCarrier): ByteArray {
        val proto = PirErrorDeclarationCarrier.newBuilder()
        proto.setLastModified(carrier.lastModified)
        carrier.parentSymbolField?.let { proto.setParentSymbol(serializeParentSymbol(it)) }
        proto.setOrigin(serializeOrigin(carrier.originField))
        proto.addAllAnnotation(carrier.annotationsField.map { serializeAnnotation(it) })
        return proto.build().toByteArray()
    }

    fun serializeFieldCarrier(carrier: FieldCarrier): ByteArray {
        val proto = PirFieldCarrier.newBuilder()
        proto.setLastModified(carrier.lastModified)
        carrier.parentSymbolField?.let { proto.setParentSymbol(serializeParentSymbol(it)) }
        proto.setOrigin(serializeOrigin(carrier.originField))
        proto.addAllAnnotation(carrier.annotationsField.map { serializeAnnotation(it) })
        proto.setType(serializeType(carrier.typeField))
        carrier.initializerField?.let { proto.setInitializer(serializeExpressionBody(it)) }
        carrier.correspondingPropertySymbolField?.let { proto.setCorrespondingPropertySymbol(serializePropertySymbol(it)) }
        return proto.build().toByteArray()
    }

    fun serializeFunctionCarrier(carrier: FunctionCarrier): ByteArray {
        val proto = PirFunctionCarrier.newBuilder()
        proto.setLastModified(carrier.lastModified)
        carrier.parentSymbolField?.let { proto.setParentSymbol(serializeParentSymbol(it)) }
        proto.setOrigin(serializeOrigin(carrier.originField))
        proto.addAllAnnotation(carrier.annotationsField.map { serializeAnnotation(it) })
        proto.setReturnTypeField(serializeType(carrier.returnTypeFieldField))
        carrier.dispatchReceiverParameterSymbolField?.let { proto.setDispatchReceiverParameter(serializeValueParameter(it)) }
        carrier.extensionReceiverParameterSymbolField?.let { proto.setExtensionReceiverParameter(serializeValueParameter(it)) }
        carrier.bodyField?.let { proto.setBody(serializeBody(it)) }
        proto.setFlags(serializeVisibility(carrier.visibilityField))
        proto.addAllTypeParameters(carrier.typeParametersSymbolField.map { serializeTypeParameter(it) })
        proto.addAllValueParameters(carrier.valueParametersSymbolField.map { serializeValueParameter(it) })
        carrier.correspondingPropertySymbolField?.let { proto.setCorrespondingPropertySymbol(serializePropertySymbol(it)) }
        proto.addAllOverriddenSymbols(carrier.overriddenSymbolsField.map { serializeSimpleFunctionSymbol(it) })
        return proto.build().toByteArray()
    }

    fun serializeLocalDelegatedPropertyCarrier(carrier: LocalDelegatedPropertyCarrier): ByteArray {
        val proto = PirLocalDelegatedPropertyCarrier.newBuilder()
        proto.setLastModified(carrier.lastModified)
        carrier.parentSymbolField?.let { proto.setParentSymbol(serializeParentSymbol(it)) }
        proto.setOrigin(serializeOrigin(carrier.originField))
        proto.addAllAnnotation(carrier.annotationsField.map { serializeAnnotation(it) })
        proto.setType(serializeType(carrier.typeField))
        carrier.delegateField?.let { proto.setDelegate(serializeVariable(it)) }
        carrier.getterSymbolField?.let { proto.setGetter(serializeSimpleFunction(it)) }
        carrier.setterSymbolField?.let { proto.setSetter(serializeSimpleFunction(it)) }
        return proto.build().toByteArray()
    }

    fun serializePropertyCarrier(carrier: PropertyCarrier): ByteArray {
        val proto = PirPropertyCarrier.newBuilder()
        proto.setLastModified(carrier.lastModified)
        carrier.parentSymbolField?.let { proto.setParentSymbol(serializeParentSymbol(it)) }
        proto.setOrigin(serializeOrigin(carrier.originField))
        proto.addAllAnnotation(carrier.annotationsField.map { serializeAnnotation(it) })
        carrier.backingFieldSymbolField?.let { proto.setBackingField(serializeField(it)) }
        carrier.getterSymbolField?.let { proto.setGetter(serializeSimpleFunction(it)) }
        carrier.setterSymbolField?.let { proto.setSetter(serializeSimpleFunction(it)) }
        return proto.build().toByteArray()
    }

    fun serializeTypeAliasCarrier(carrier: TypeAliasCarrier): ByteArray {
        val proto = PirTypeAliasCarrier.newBuilder()
        proto.setLastModified(carrier.lastModified)
        carrier.parentSymbolField?.let { proto.setParentSymbol(serializeParentSymbol(it)) }
        proto.setOrigin(serializeOrigin(carrier.originField))
        proto.addAllAnnotation(carrier.annotationsField.map { serializeAnnotation(it) })
        proto.addAllTypeParameters(carrier.typeParametersSymbolField.map { serializeTypeParameter(it) })
        proto.setExpandedType(serializeType(carrier.expandedTypeField))
        return proto.build().toByteArray()
    }

    fun serializeTypeParameterCarrier(carrier: TypeParameterCarrier): ByteArray {
        val proto = PirTypeParameterCarrier.newBuilder()
        proto.setLastModified(carrier.lastModified)
        carrier.parentSymbolField?.let { proto.setParentSymbol(serializeParentSymbol(it)) }
        proto.setOrigin(serializeOrigin(carrier.originField))
        proto.addAllAnnotation(carrier.annotationsField.map { serializeAnnotation(it) })
        proto.addAllSuperTypes(carrier.superTypesField.map { serializeSuperType(it) })
        return proto.build().toByteArray()
    }

    fun serializeValueParameterCarrier(carrier: ValueParameterCarrier): ByteArray {
        val proto = PirValueParameterCarrier.newBuilder()
        proto.setLastModified(carrier.lastModified)
        carrier.parentSymbolField?.let { proto.setParentSymbol(serializeParentSymbol(it)) }
        proto.setOrigin(serializeOrigin(carrier.originField))
        proto.addAllAnnotation(carrier.annotationsField.map { serializeAnnotation(it) })
        carrier.defaultValueField?.let { proto.setDefaultValue(serializeExpressionBody(it)) }
        proto.setType(serializeType(carrier.typeField))
        carrier.varargElementTypeField?.let { proto.setVarargElementType(serializeType(it)) }
        return proto.build().toByteArray()
    }
}
