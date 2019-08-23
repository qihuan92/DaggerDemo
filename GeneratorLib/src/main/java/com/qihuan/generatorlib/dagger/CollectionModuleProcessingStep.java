package com.qihuan.generatorlib.dagger;

import com.google.auto.common.BasicAnnotationProcessor;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.SetMultimap;
import com.qihuan.annotationlib.dagger.AutoModule;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;

import dagger.Module;

import static com.qihuan.generatorlib.dagger.Const.COLLECTION_MODULE_NAME;
import static com.qihuan.generatorlib.dagger.Const.DAGGER_GEN_PACKAGE;

public class CollectionModuleProcessingStep implements BasicAnnotationProcessor.ProcessingStep {

    private Filer filer;

    public CollectionModuleProcessingStep(Filer filer) {
        this.filer = filer;
    }

    @Override
    public Set<? extends Class<? extends Annotation>> annotations() {
        return ImmutableSet.of(
                AutoModule.class
        );
    }

    @Override
    public Set<? extends Element> process(SetMultimap<Class<? extends Annotation>, Element> setMultimap) {
        List<Element> elementList = new ArrayList<>(setMultimap.values());
        if (elementList.size() != 0) {
            generate(elementList);
        }
        return ImmutableSet.of();
    }

    private void generate(List<Element> elementList) {
        AnnotationSpec.Builder annotationSpecBuilder = AnnotationSpec.builder(Module.class);
        for (Element element : elementList) {
            annotationSpecBuilder.addMember("includes", "$T.class", ClassName.get(element.asType()));
        }
        AnnotationSpec annotationSpec = annotationSpecBuilder.build();

        JavaFile javaFile = JavaFile.builder(
                DAGGER_GEN_PACKAGE,
                TypeSpec.classBuilder(COLLECTION_MODULE_NAME)
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .addAnnotation(annotationSpec)
                        .build()
        ).build();
        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
