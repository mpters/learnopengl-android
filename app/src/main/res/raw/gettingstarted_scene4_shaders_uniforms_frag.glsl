#version 300 es
precision mediump float;

out vec4 FragColor;

uniform vec4 ourColor;

void main() {
    FragColor = ourColor;
}
