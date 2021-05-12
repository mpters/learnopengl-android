#version 300 es
precision mediump float;

in vec2 TexCoords;
out vec4 color;

uniform sampler2D scene;
uniform vec2 offsets[9];
uniform int edge_kernel[9];
uniform float  blur_kernel[9];

uniform bool chaos;
uniform bool confuse;
uniform bool shake;

void main() {
    // zero out memory since an out variable is initialized with undefined values by default
    color = vec4(0.0f);

    vec3 smple[9];

    // sample from texture offsets if using convolution matrix
    if (chaos || shake) {
        for (int i = 0; i < 9; i++) {
            smple[i] = vec3(texture(scene, TexCoords.st + offsets[i]));
        }
    }

    // process effects
    if (chaos) {
        for (int i = 0; i < 9; i++) {
            color += vec4(smple[i] * float(edge_kernel[i]), 0.0f);
        }
        color.a = 1.0f;
    } else if (confuse) {
        color = vec4(1.0 - texture(scene, TexCoords).rgb, 1.0);
    } else if (shake) {
        for (int i = 0; i < 9; i++) {
            color += vec4(smple[i] * blur_kernel[i], 0.0f);
        }
        color.a = 1.0f;
    } else {
        color =  texture(scene, TexCoords);
    }
}
