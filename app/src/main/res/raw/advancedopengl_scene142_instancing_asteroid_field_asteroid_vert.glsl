#version 300 es
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec2 aTexCoords;
layout (location = 2) in mat4 instanceMatrix;

out vec3 Normal;
out vec2 TexCoords;

uniform mat4 projection;
uniform mat4 view;

void main() {
    TexCoords = aTexCoords;
    gl_Position = projection * view * instanceMatrix * vec4(aPos, 1.0);
}
