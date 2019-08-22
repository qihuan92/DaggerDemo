package com.qihuan.generatorlib.dagger;

import com.google.auto.common.BasicAnnotationProcessor;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.SetMultimap;
import com.qihuan.annotationlib.dagger.AutoAppBinding;
import com.qihuan.annotationlib.dagger.AutoModule;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.util.Elements;

import dagger.Binds;
import dagger.Module;

public class AppModuleProcessingStep implements BasicAnnotationProcessor.ProcessingStep {

    private Filer filer;
    private Elements elements;

    public AppModuleProcessingStep(Filer filer, Elements elements) {
        this.filer = filer;
        this.elements = elements;
    }

    @Override
    public Set<? extends Class<? extends Annotation>> annotations() {
        return ImmutableSet.of(
                AutoAppBinding.class
        );
    }

    @Override
    public Set<? extends Element> process(SetMultimap<Class<? extends Annotation>, Element> setMultimap) {
        Optional<Element> element = setMultimap.values().stream().findFirst();
        element.ifPresent(this::generate);
        return ImmutableSet.of(element.get());
    }

    private void generate(Element element) {
        String packageName = elements.getPackageOf(element).toString() + ".di";
        JavaFile javaFile = JavaFile.builder(
                packageName,
                TypeSpec.classBuilder("AppModule")
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .addAnnotation(AutoModule.class)
                        .addAnnotation(Module.class)
                        .addMethod(
                                MethodSpec.methodBuilder("inject" + element.getSimpleName().toString())
                                        .addModifiers(Modifier.ABSTRACT)
                                        .addAnnotation(Binds.class)
                                        .addParameter(TypeName.get(element.asType()), element.getSimpleName().toString().toLowerCase())
                                        .returns(ClassName.get("android.app", "Application"))
                                        .build()
                        )
                        .build()
        ).build();

        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
