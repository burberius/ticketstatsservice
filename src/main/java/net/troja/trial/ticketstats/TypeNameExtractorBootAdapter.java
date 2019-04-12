package net.troja.trial.ticketstats;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;
import com.fasterxml.classmate.types.ResolvedArrayType;
import com.fasterxml.classmate.types.ResolvedObjectType;
import com.fasterxml.classmate.types.ResolvedPrimitiveType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Component;
import springfox.documentation.schema.DefaultTypeNameProvider;
import springfox.documentation.schema.ModelNameContext;
import springfox.documentation.schema.TypeNameExtractor;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.schema.EnumTypeDeterminer;
import springfox.documentation.spi.schema.GenericTypeNamingStrategy;
import springfox.documentation.spi.schema.TypeNameProviderPlugin;
import springfox.documentation.spi.schema.contexts.ModelContext;

import java.lang.reflect.Type;
import java.util.Locale;

import static springfox.documentation.schema.Collections.containerType;
import static springfox.documentation.schema.Collections.isContainerType;
import static springfox.documentation.schema.Types.typeNameFor;


/**
 * This class is used to fix incompatibilities between Springfox and Spring Boot 2.2
 * see https://github.com/springfox/springfox/issues/2932
 */
@Component
@Primary
public class TypeNameExtractorBootAdapter extends TypeNameExtractor {
    private final TypeResolver typeResolver;
    private final PluginRegistry<TypeNameProviderPlugin, DocumentationType> typeNameProviders;
    private final EnumTypeDeterminer enumTypeDeterminer;

    @Autowired
    public TypeNameExtractorBootAdapter(TypeResolver typeResolver,
                                        PluginRegistry<TypeNameProviderPlugin, DocumentationType> typeNameProviders,
                                        EnumTypeDeterminer enumTypeDeterminer) {
        super(typeResolver, typeNameProviders, enumTypeDeterminer);
        this.typeResolver = typeResolver;
        this.typeNameProviders = typeNameProviders;
        this.enumTypeDeterminer = enumTypeDeterminer;
    }

    public String typeName(ModelContext context) {
        ResolvedType type = asResolved(context.getType());
        if (isContainerType(type)) {
            return containerType(type);
        }
        return innerTypeName(type, context);
    }

    private ResolvedType asResolved(Type type) {
        return typeResolver.resolve(type);
    }

    private String genericTypeName(ResolvedType resolvedType, ModelContext context) {
        Class<?> erasedType = resolvedType.getErasedType();
        GenericTypeNamingStrategy namingStrategy = context.getGenericNamingStrategy();
        ModelNameContext nameContext = new ModelNameContext(resolvedType.getErasedType(),
                context.getDocumentationType());
        String simpleName = typeNameFor(erasedType);
        if(simpleName == null)
            simpleName = typeName(nameContext);
        StringBuilder sb = new StringBuilder(String.format(Locale.ENGLISH, "%s%s", simpleName, namingStrategy.getOpenGeneric()));
        boolean first = true;
        for (int index = 0; index < erasedType.getTypeParameters().length; index++) {
            ResolvedType typeParam = resolvedType.getTypeParameters().get(index);
            if (first) {
                sb.append(innerTypeName(typeParam, context));
                first = false;
            } else {
                sb.append(String.format(Locale.ENGLISH, "%s%s", namingStrategy.getTypeListDelimiter(),
                        innerTypeName(typeParam, context)));
            }
        }
        sb.append(namingStrategy.getCloseGeneric());
        return sb.toString();
    }

    private String innerTypeName(ResolvedType type, ModelContext context) {
        if (type.getTypeParameters().size() > 0 && type.getErasedType().getTypeParameters().length > 0) {
            return genericTypeName(type, context);
        }
        return simpleTypeName(type, context);
    }

    private String simpleTypeName(ResolvedType type, ModelContext context) {
        Class<?> erasedType = type.getErasedType();
        if (type instanceof ResolvedPrimitiveType) {
            return typeNameFor(erasedType);
        } else if (enumTypeDeterminer.isEnum(erasedType)) {
            return "string";
        } else if (type instanceof ResolvedArrayType) {
            GenericTypeNamingStrategy namingStrategy = context.getGenericNamingStrategy();
            return String.format(Locale.ENGLISH, "Array%s%s%s", namingStrategy.getOpenGeneric(),
                    simpleTypeName(type.getArrayElementType(), context), namingStrategy.getCloseGeneric());
        } else if (type instanceof ResolvedObjectType) {
            String typeName = typeNameFor(erasedType);
            if (typeName != null) {
                return typeName;
            }
        }
        return typeName(new ModelNameContext(type.getErasedType(), context.getDocumentationType()));
    }

    private String typeName(ModelNameContext context) {
        TypeNameProviderPlugin selected = typeNameProviders.getPluginOrDefaultFor(context.getDocumentationType(),
                new DefaultTypeNameProvider());
        return selected.nameFor(context.getType());
    }
}

