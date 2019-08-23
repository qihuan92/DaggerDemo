package com.qihuan.generatorlib;

import com.google.auto.common.BasicAnnotationProcessor;
import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.qihuan.generatorlib.dagger.AndroidBindingModuleProcessingStep;
import com.qihuan.generatorlib.dagger.AppComponentProcessingStep;
import com.qihuan.generatorlib.dagger.CollectionModuleProcessingStep;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Processor;
import javax.lang.model.SourceVersion;
import javax.lang.model.util.Elements;

@SuppressWarnings("unused")
@AutoService(Processor.class)
public class AutoDaggerProcessor extends BasicAnnotationProcessor {

    @Override
    protected Iterable<? extends ProcessingStep> initSteps() {
        Filer filer = processingEnv.getFiler();
        Elements elements = processingEnv.getElementUtils();
        return ImmutableList.of(
                new AndroidBindingModuleProcessingStep(filer),
                new CollectionModuleProcessingStep(filer),
                new AppComponentProcessingStep(filer, elements)
        );
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
