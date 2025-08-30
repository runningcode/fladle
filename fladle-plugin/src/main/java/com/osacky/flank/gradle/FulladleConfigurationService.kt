package com.osacky.flank.gradle

import com.android.build.gradle.TestedExtension
import org.gradle.api.Project

/**
 * Service responsible for collecting project information at configuration time.
 * This enables configuration cache compatibility by collecting data when Project objects
 * are available, then storing it in serializable structures.
 */
class FulladleConfigurationService {

    /**
     * Collects module information from all subprojects at configuration time.
     * This data will be serialized and used during task execution without requiring
     * live Project object references.
     */
    fun collectModuleInformation(rootProject: Project): List<ModuleInfo> {
        return rootProject.subprojects
            .filter { it.hasAndroidTest }
            .map { project ->
                val moduleExtension = project.extensions.findByType(FulladleModuleExtension::class.java)
                val variants = collectVariantInformation(project)
                
                ModuleInfo(
                    projectPath = project.path,
                    isAndroidApp = project.isAndroidAppModule,
                    isAndroidLibrary = project.isAndroidLibraryModule,
                    hasTests = project.hasAndroidTest,
                    enabled = moduleExtension?.enabled?.get() ?: true,
                    config = SerializableModuleConfig(
                        maxTestShards = moduleExtension?.maxTestShards?.orNull,
                        clientDetails = moduleExtension?.clientDetails?.get() ?: emptyMap(),
                        environmentVariables = moduleExtension?.environmentVariables?.get() ?: emptyMap(),
                        debugApk = moduleExtension?.debugApk?.orNull,
                        variant = moduleExtension?.variant?.orNull
                    ),
                    variants = variants
                )
            }
    }

    /**
     * Collects Android variant information from a project.
     * This includes both test variants and their corresponding outputs.
     */
    private fun collectVariantInformation(project: Project): List<VariantInfo> {
        val testedExtension = project.extensions.findByType(TestedExtension::class.java) 
            ?: return emptyList()

        return try {
            testedExtension.testVariants.map { testVariant ->
                val testedVariant = testVariant.testedVariant
                
                VariantInfo(
                    name = testVariant.name,
                    testedVariantName = testedVariant.name,
                    outputs = testedVariant.outputs.map { output ->
                        VariantOutputInfo(
                            outputFilePath = output.outputFile.absolutePath,
                            filterType = output.filters.firstOrNull()?.filterType,
                            identifier = output.filters.firstOrNull()?.identifier
                        )
                    },
                    testOutputs = testVariant.outputs.map { output ->
                        VariantOutputInfo(
                            outputFilePath = output.outputFile.absolutePath,
                            filterType = output.filters.firstOrNull()?.filterType,
                            identifier = output.filters.firstOrNull()?.identifier
                        )
                    }
                )
            }
        } catch (e: Exception) {
            // If variants aren't ready yet, return empty list
            // This will be populated later in afterEvaluate
            emptyList()
        }
    }
}