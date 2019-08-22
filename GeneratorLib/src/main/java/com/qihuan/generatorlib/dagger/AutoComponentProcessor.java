package com.qihuan.generatorlib.dagger;

import com.google.auto.service.AutoService;
import com.qihuan.annotationlib.dagger.AutoBinding;
import com.qihuan.annotationlib.dagger.AutoModule;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

/**
 * AutoComponentProcessor
 *
 * @author qi
 * @date 2019-08-21
 */
@SuppressWarnings("unused")
@AutoService(Processor.class)
public class AutoComponentProcessor extends AbstractProcessor {

    private Filer filer;
    private Messager messager;
    private List<Element> autoBindingElementList;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        autoBindingElementList = new ArrayList<>();
        filer = processingEnvironment.getFiler();
        messager = processingEnvironment.getMessager();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        LinkedHashSet<String> annotations = new LinkedHashSet<>();
        annotations.add(AutoBinding.class.getCanonicalName());
        return annotations;
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        for (Element element : roundEnvironment.getElementsAnnotatedWith(AutoBinding.class)) {
            if (element.getKind() != ElementKind.CLASS) {
                error(element, "Only classes can be annotated with @%s", AutoBinding.class.getSimpleName());
                return true;
            }
            autoBindingElementList.add(element);
        }
        genCode();
        return false;
    }

    private void genCode() {
        if (autoBindingElementList.size() == 0) {
            return;
        }
        // android binding
        JavaFile androidBindingFile = androidBindingFile(autoBindingElementList);
        try {
            androidBindingFile.writeTo(filer);
        } catch (IOException e) {
            note(null, e.getMessage());
        }
    }

    private JavaFile androidBindingFile(List<Element> elementList) {
        List<MethodSpec> methodSpecList = new ArrayList<>();
        for (Element element : elementList) {
            // 获取注解中的 module
            AutoBinding annotation = element.getAnnotation(AutoBinding.class);
            List<TypeMirror> moduleTypeMirrorList = new ArrayList<>();
            try {
                annotation.modules();
            } catch (MirroredTypesException e) {
                if (e.getTypeMirrors() != null) {
                    moduleTypeMirrorList.addAll(e.getTypeMirrors());
                }
            }
            AnnotationSpec.Builder annotationSpecBuilder = AnnotationSpec.builder(ContributesAndroidInjector.class);
            for (TypeMirror typeMirror : moduleTypeMirrorList) {
                annotationSpecBuilder.addMember("modules", "$T.class", ClassName.get(typeMirror));
            }
            AnnotationSpec annotationSpec = annotationSpecBuilder.build();
            // inject 函数
            methodSpecList.add(
                    MethodSpec.methodBuilder("inject" + element.getSimpleName().toString())
                            .addModifiers(Modifier.ABSTRACT)
                            .addAnnotation(annotationSpec)
                            .returns(TypeName.get(element.asType()))
                            .build()
            );
        }
        return JavaFile.builder(
                "com.qihuan.dagger",
                TypeSpec.classBuilder("AndroidBindingModule")
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .addAnnotation(AutoModule.class)
                        .addAnnotation(Module.class)
                        .addMethods(methodSpecList)
                        .build()
        ).build();
    }

    @SuppressWarnings({"SameParameterValue", "unused"})
    private void error(Element e, String msg, Object... args) {
        messager.printMessage(Diagnostic.Kind.ERROR, String.format(msg, args), e);
    }

    @SuppressWarnings({"SameParameterValue", "unused"})
    private void note(Element e, String msg, Object... args) {
        messager.printMessage(Diagnostic.Kind.NOTE, String.format(msg, args), e);
    }
}
