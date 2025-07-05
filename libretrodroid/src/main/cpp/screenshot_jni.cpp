#define STB_IMAGE_WRITE_IMPLEMENTATION
#include "utils/stb_image_write.h"
#include <jni.h>
#include <GLES2/gl2.h>
#include <vector>
#include <cstring>
#include <android/log.h>
#include "libretrodroid.h"

// Helper pour retourner l'image verticalement (OpenGL -> Android)
void flipVertical(uint8_t* data, int width, int height) {
    int stride = width * 4;
    std::vector<uint8_t> tmp(stride);
    for (int y = 0; y < height / 2; ++y) {
        uint8_t* row1 = data + y * stride;
        uint8_t* row2 = data + (height - 1 - y) * stride;
        memcpy(tmp.data(), row1, stride);
        memcpy(row1, row2, stride);
        memcpy(row2, tmp.data(), stride);
    }
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_swordfish_libretrodroid_GLRetroView_captureScreenshot256x240(JNIEnv* env, jobject thiz, jint surfaceWidth, jint surfaceHeight) {
    const int width = 256;
    const int height = 240;

    // Logs de diagnostic pour identifier le vrai format
    __android_log_print(ANDROID_LOG_INFO, "SCREENSHOT_DEBUG", "=== DIAGNOSTIC CAPTURE ===");
    __android_log_print(ANDROID_LOG_INFO, "SCREENSHOT_DEBUG", "Paramètres demandés: %dx%d", width, height);
    __android_log_print(ANDROID_LOG_INFO, "SCREENSHOT_DEBUG", "Surface réelle: %dx%d", surfaceWidth, surfaceHeight);
    
    // Vérifier le framebuffer actuel
    GLint currentFramebuffer;
    glGetIntegerv(GL_FRAMEBUFFER_BINDING, &currentFramebuffer);
    __android_log_print(ANDROID_LOG_INFO, "SCREENSHOT_DEBUG", "Framebuffer actuel: %d", currentFramebuffer);
    
    // Vérifier le viewport
    GLint viewport[4];
    glGetIntegerv(GL_VIEWPORT, viewport);
    __android_log_print(ANDROID_LOG_INFO, "SCREENSHOT_DEBUG", "Viewport: %d,%d,%d,%d", viewport[0], viewport[1], viewport[2], viewport[3]);
    
    // Test avec différentes tailles
    __android_log_print(ANDROID_LOG_INFO, "SCREENSHOT_DEBUG", "Test capture %dx%d à (0,0)", surfaceWidth, surfaceHeight);
    
    // Utiliser la taille réelle du framebuffer pour la capture et l'encodage PNG
    int captureWidth = surfaceWidth;
    int captureHeight = surfaceHeight;
    __android_log_print(ANDROID_LOG_INFO, "SCREENSHOT_DEBUG", "Capture FULL: %dx%d à (0,0)", captureWidth, captureHeight);
    std::vector<uint8_t> pixels(captureWidth * captureHeight * 4); // RGBA
    glBindFramebuffer(GL_FRAMEBUFFER, 0);
    glReadPixels(0, 0, captureWidth, captureHeight, GL_RGBA, GL_UNSIGNED_BYTE, pixels.data());
    __android_log_print(ANDROID_LOG_INFO, "SCREENSHOT_DEBUG", "Capture terminée, pixels: %zu", pixels.size());
    
    // Vérifier si les pixels ont été lus correctement
    int totalPixels = captureWidth * captureHeight;
    int nonZeroPixels = 0;
    for (int i = 0; i < totalPixels * 4; i += 4) {
        if (pixels[i] > 0 || pixels[i+1] > 0 || pixels[i+2] > 0) {
            nonZeroPixels++;
        }
    }
    __android_log_print(ANDROID_LOG_DEBUG, "SCREENSHOT", "Pixels lus depuis l'écran: %d/%d pixels non-noirs", nonZeroPixels, totalPixels);
    
    // Si tous les pixels sont noirs, essayer de capturer depuis le centre de l'écran
    if (nonZeroPixels == 0) {
        __android_log_print(ANDROID_LOG_WARN, "SCREENSHOT", "Tous les pixels sont noirs, essai depuis le centre");
        int centerX = (surfaceWidth - captureWidth) / 2;
        int centerY = (surfaceHeight - captureHeight) / 2;
        glReadPixels(centerX, centerY, captureWidth, captureHeight, GL_RGBA, GL_UNSIGNED_BYTE, pixels.data());
        
        // Vérifier à nouveau
        nonZeroPixels = 0;
        for (int i = 0; i < totalPixels * 4; i += 4) {
            if (pixels[i] > 0 || pixels[i+1] > 0 || pixels[i+2] > 0) {
                nonZeroPixels++;
            }
        }
        __android_log_print(ANDROID_LOG_DEBUG, "SCREENSHOT", "Pixels lus depuis le centre: %d/%d pixels non-noirs", nonZeroPixels, totalPixels);
    }
    
    // Si toujours tous les pixels sont noirs, utiliser l'image de test
    if (nonZeroPixels == 0) {
        __android_log_print(ANDROID_LOG_WARN, "SCREENSHOT", "Tous les pixels sont noirs, utilisation de l'image de test");
        for (int i = 0; i < captureWidth * captureHeight; ++i) {
            int x = i % captureWidth;
            int y = i / captureWidth;
            pixels[i * 4 + 0] = (x * 255) / captureWidth;     // R - gradient horizontal
            pixels[i * 4 + 1] = (y * 255) / captureHeight;    // G - gradient vertical  
            pixels[i * 4 + 2] = 128;                   // B - bleu fixe
            pixels[i * 4 + 3] = 255;                   // A - opaque
        }
    }
    
    // Retourner l'image dans le bon sens
    flipVertical(pixels.data(), captureWidth, captureHeight);

    // Encode PNG en mémoire
    std::vector<uint8_t> pngData;
    stbi_write_png_to_func(
        [](void* context, void* data, int size) {
            auto* vec = reinterpret_cast<std::vector<uint8_t>*>(context);
            vec->insert(vec->end(), (uint8_t*)data, (uint8_t*)data + size);
        },
        &pngData, captureWidth, captureHeight, 4, pixels.data(), captureWidth * 4
    );

    // DEBUG : log la taille du buffer PNG généré
    __android_log_print(ANDROID_LOG_DEBUG, "SCREENSHOT", "PNG généré : %zu octets", pngData.size());
    if (pngData.size() == 0) {
        __android_log_print(ANDROID_LOG_ERROR, "SCREENSHOT", "ERREUR : PNG buffer vide !");
    }

    // Retourne le PNG à Java
    jbyteArray result = env->NewByteArray(pngData.size());
    env->SetByteArrayRegion(result, 0, pngData.size(), (jbyte*)pngData.data());
    return result;
} 