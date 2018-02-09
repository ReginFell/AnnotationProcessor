package processor.annotation.presentation.processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

import processor.annotation.presentation.annotation.ViewState;
import processor.annotation.presentation.core.BaseViewState;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("processor.annotation.presentation.annotation.ViewState")
@AutoService(Processor.class)
public class ViewStateProcessor extends AbstractProcessor {

    private Elements elementUtils;
    private Filer filer;
    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        elementUtils = processingEnvironment.getElementUtils();
        filer = processingEnvironment.getFiler();
        messager = processingEnv.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        for (Element element : roundEnvironment.getElementsAnnotatedWith(ViewState.class)) {
            if (element.getKind() == ElementKind.INTERFACE) {

                final String packageName = elementUtils.getPackageOf(element).getQualifiedName().toString();
                final String className = element.getSimpleName() + "State";

                final TypeSpec.Builder outputType = TypeSpec.classBuilder(className)
                        .addSuperinterface(TypeName.get(element.asType()))
                        .superclass(ParameterizedTypeName.get(ClassName.get(BaseViewState.class), TypeName.get(element.asType())))
                        .addModifiers(Modifier.PUBLIC);

                for (Element method : element.getEnclosedElements()) {
                    if (method.getKind() == ElementKind.METHOD) {
                        final ExecutableType executableType = (ExecutableType) method.asType();

                        final List<ParameterSpec> parameterSpecs = createParameterSpecs(executableType.getParameterTypes());
                        final String argumentsString = extractArguments(parameterSpecs);

                        CodeBlock codeBlock;
                        if (TypeName.get(executableType.getReturnType()).equals(TypeName.VOID)) {
                            codeBlock = CodeBlock.builder()
                                    .add("dispatch(view -> view.")
                                    .add(method.getSimpleName().toString() + "(" + argumentsString + ")")
                                    .add(");")
                                    .build();
                        } else {
                            codeBlock = CodeBlock.builder()
                                    .add("return processUnsafe(view -> view.")
                                    .add(method.getSimpleName().toString() + "(" + argumentsString + ")")
                                    .add(");")
                                    .build();
                        }

                        outputType.addMethod(MethodSpec.methodBuilder(method.getSimpleName().toString())
                                .addParameters(parameterSpecs)
                                .addModifiers(Modifier.PUBLIC)
                                .returns(TypeName.get(executableType.getReturnType()))
                                .addCode(codeBlock)
                                .build());
                    }
                }

                JavaFile javaFile = JavaFile.builder(packageName, outputType.build())
                        .addFileComment("This file is auto-generated and should not be edited.")
                        .build();
                try {
                    javaFile.writeTo(filer);
                } catch (IOException e) {
                    messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING, e.getMessage(), element);
                }
            }
        }
        return true;
    }

    private List<ParameterSpec> createParameterSpecs(List<? extends TypeMirror> parameters) {
        return IntStream.range(0, parameters.size())
                .boxed()
                .map(position -> {
                    final TypeMirror mirror = parameters.get(position);
                    return ParameterSpec.builder(TypeName.get(mirror), "arg" + position).build();
                })
                .collect(Collectors.toList());
    }

    private String extractArguments(List<ParameterSpec> parameterSpecs) {
        return parameterSpecs.stream()
                .map(parameterSpec -> parameterSpec.name)
                .collect(Collectors.joining(", "));
    }
}