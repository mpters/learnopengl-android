#version 300 es
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNormal;
layout (location = 2) in vec2 aTexCoords;

out vec3 FragPos;
out vec3 Normal;
out vec2 TexCoords;

uniform mat4 projection;
uniform mat4 view;
uniform mat4 model;
uniform bool reverse_normals;

void main() {
    FragPos = vec3(model * vec4(aPos, 1.0));
    if (reverse_normals) { // a slight hack to make sure the outer large cube displays lighting from the 'inside' instead of the default 'outside'.
        Normal = transpose(inverse(mat3(model))) * (-1.0 * aNormal);
    } else {
        Normal = transpose(inverse(mat3(model))) * aNormal;
    }
    TexCoords = aTexCoords;
    gl_Position = projection * view * model * vec4(aPos, 1.0);
}
