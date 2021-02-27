#version 300 es
precision mediump float;

out vec4 FragColor;
in vec2 TexCoords;

uniform sampler2D screenTexture;

const float offset = 1.0 / 300.0;

void main() {
    vec2 offsets[9] = vec2[](
    vec2(-offset, offset), // top-left
    vec2(0.0, offset), // top-center
    vec2(offset, offset), // top-right
    vec2(-offset, 0.0), // center-left
    vec2(0.0, 0.0), // center-center
    vec2(offset, 0.0), // center-right
    vec2(-offset, -offset), // bottom-left
    vec2(0.0, -offset), // bottom-center
    vec2(offset, -offset)// bottom-right
    );

    // Edge detection
    float kernel[9] = float[](
    1.0, 1.0, 1.0,
    1.0, -8.0, 1.0,
    1.0, 1.0, 1.0
    );

    vec3 sampleTex[9];
    for (int i = 0; i < 9; i++) {
        sampleTex[i] = vec3(texture(screenTexture, TexCoords.st + offsets[i]));
    }
    vec3 col = vec3(0.0);
    for (int i = 0; i < 9; i++) {
        col += sampleTex[i] * kernel[i];
    }

    FragColor = vec4(col, 1.0);
}
