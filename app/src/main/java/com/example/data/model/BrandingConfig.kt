package com.example.data.model

/**
 * Branding Configuration for Zaid AI Studio
 *
 * Python Reference:
 * class BrandingConfig:
 *     APP_NAME = "زايد للمونتاج التلقائي بالذكاء الاصطناعي"
 *     BRAND_TAG = "Zaid AI Studio"
 *     WATERMARK_PATH = "assets/branding/zaid_logo_metallic.png"
 *     WATERMARK_DEFAULT_ENABLED = True  # إجباري ومثبت في كل عمليات التصدير
 *     WATERMARK_OPACITY = 0.85          # نسبة شفافية سينمائية 85%
 */
object BrandingConfig {
    const val APP_NAME = "زايد للمونتاج التلقائي بالذكاء الاصطناعي"
    const val BRAND_TAG = "Zaid AI Studio"
    const val WATERMARK_PATH = "assets/branding/zaid_logo_metallic.png"
    const val WATERMARK_DEFAULT_ENABLED = true  // إجباري ومثبت في كل عمليات التصدير
    const val WATERMARK_OPACITY = 0.85f         // نسبة شفافية سينمائية 85%

    /**
     * Python representation of the BrandingConfig specification
     */
    val pythonClassDefinition: String = """
class BrandingConfig:
    APP_NAME = "زايد للمونتاج التلقائي بالذكاء الاصطناعي"
    BRAND_TAG = "Zaid AI Studio"
    WATERMARK_PATH = "assets/branding/zaid_logo_metallic.png"
    WATERMARK_DEFAULT_ENABLED = True  # إجباري ومثبت في كل عمليات التصدير
    WATERMARK_OPACITY = 0.85          # نسبة شفافية سينمائية 85%
""".trimIndent()
}
