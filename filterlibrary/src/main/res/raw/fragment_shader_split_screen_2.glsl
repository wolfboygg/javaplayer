precision highp float;

uniform sampler2D sTexture;
varying highp vec2 ft_Position;

void main() {
    vec2 uv = ft_Position.xy;
    float y;
    if (uv.y >= 0.0 && uv.y <= 0.5) {
        y = uv.y + 0.25;
    } else {
        y = uv.y - 0.25;
    }
    gl_FragColor=texture2D(sTexture, vec2(uv.x, y));
}

