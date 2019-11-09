precision highp float;

uniform sampler2D sTexture;
varying highp vec2 ft_Position;
const highp vec3 W = vec3(0.2125, 0.7154, 0.0721);

void main() {
    vec2 uv = ft_Position.xy;
    float y;
    if (uv.y >= 0.0 && uv.y <= 0.5) {
        y = uv.y + 0.25;
    } else {
        y = uv.y - 0.25;
    }

    vec4 mask = texture2D(sTexture, ft_Position);
    float temp = dot(mask.rgb, W);
    gl_FragColor = vec4(vec3(temp), 1.0);
}

