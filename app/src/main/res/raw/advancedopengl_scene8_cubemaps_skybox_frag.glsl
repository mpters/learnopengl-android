#version 300 es
precision mediump float;

out vec4 FragColor;
in vec3 TexCoords;

uniform samplerCube skybox;

void main() {
    FragColor = texture(skybox, TexCoords);
}
