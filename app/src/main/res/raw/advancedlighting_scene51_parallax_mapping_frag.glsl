#version 300 es
precision mediump float;

out vec4 FragColor;
in vec3 FragPos;
in vec2 TexCoords;
in vec3 TangentFragPos;
in vec3 TangentLightPos;
in vec3 TangentViewPos;

uniform sampler2D diffuseMap;
uniform sampler2D normalMap;
uniform sampler2D depthMap;
uniform float heightScale;

vec2 ParallaxMapping(vec2 texCoords, vec3 viewDir) {
    float height = texture(depthMap, texCoords).r;
    return texCoords - viewDir.xy * (height * heightScale);
}

void main() {
    // offset texture coordinates with Parallax Mapping
    vec3 viewDir = normalize(TangentViewPos - TangentFragPos);
    vec2 texCoords = TexCoords;

    texCoords = ParallaxMapping(TexCoords, viewDir);
    if (texCoords.x > 1.0 || texCoords.y > 1.0 || texCoords.x < 0.0 || texCoords.y < 0.0) {
        discard;
    }

    // obtain normal from normal map
    vec3 normal = texture(normalMap, texCoords).rgb;
    normal = normalize(normal * 2.0 - 1.0);

    // get diffuse color
    vec3 color = texture(diffuseMap, texCoords).rgb;
    // ambient
    vec3 ambient = 0.1 * color;
    // diffuse
    vec3 lightDir = normalize(TangentLightPos - TangentFragPos);
    float diff = max(dot(lightDir, normal), 0.0);
    vec3 diffuse = diff * color;
    // specular
    vec3 reflectDir = reflect(-lightDir, normal);
    vec3 halfwayDir = normalize(lightDir + viewDir);
    float spec = pow(max(dot(normal, halfwayDir), 0.0), 32.0);

    vec3 specular = vec3(0.2) * spec;
    FragColor = vec4(ambient + diffuse + specular, 1.0);
}
