package io.github.goodberry_gobblers.dragonterritory.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import io.github.goodberry_gobblers.dragonterritory.DragonTerritory;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import org.joml.*;

import java.lang.Math;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class SilhouetteRenderer {

    public static int maxDragonCount = 4_000;
    public static int dead_zone_radius = 5_000;

    static RandomSource randomSource = RandomSource.create(100);
    static List<Silhouette> silhouetteList = generateSilhouettes(randomSource);

    public static void renderEntireBatch(LevelRenderer levelRenderer, PoseStack poseStack, int renderTick, Camera camera, float partialTick) {
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();

//        RenderSystem.depthMask(true);

        poseStack.pushPose();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        Matrix4f matrix4f = poseStack.last().pose();

        RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
        RenderSystem.setShaderTexture(0, new ResourceLocation(DragonTerritory.MOD_ID, "textures/silhouette_animated_placeholder.png"));
        FogRenderer.levelFogColor();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        int dragonRenderCount = dragonCount(camera.getPosition().horizontalDistance());
        List<Silhouette> silhouetteSubList = silhouetteList.subList(0, dragonRenderCount);
        silhouetteSubList.sort(Comparator.comparing(o -> -o.height));

        for (int i = 0; i < dragonRenderCount; ++i) {
            Silhouette silhouette = silhouetteSubList.get(i);
            renderAt(
                    bufferbuilder,
                    matrix4f,
                    silhouette.size,
                    silhouette.offsetAngle,
                    silhouette.radius,
                    silhouette.ellipsesRotation,
                    silhouette.ellipticalWidth,
                    silhouette.centerOffset,
                    silhouette.height,
                    silhouette.blocksPerSecond,
                    silhouette.clockwise,
                    new Vector3f(0.04f, 0.01f, 0f), //looks scarier with a solid color. Biome dependent color?
                    renderTick,
                    partialTick);
        }

        BufferUploader.drawWithShader(bufferbuilder.end());

        poseStack.popPose();
    }

    private static int getFrameId(int size, float blocksPerSecond, float offset, int renderTick) {
        int x = (int) (renderTick / 4f + offset);

        // number of consecutive flaps to do every time a dragon flaps it's wings. Each flap takes 16 ticks
        int flapCount = 6 - size;
        // how much time should elapse between the starts of flapping its wings. flapFrequency is measured in 4-ticks.
        int flapFrequency = (int) (size + (blocksPerSecond-9f) / 10f);

        // \left\{\operatorname{mod}\left(x,4f\right)<4c:2\sin^{2}\left(\frac{\pi}{4}x\right),0\right\}
        // toss that ^^^^ into desmos and add the variables 'f' and 'c' for "frequency" and "count".
        double thing = Math.sin(x * Math.PI/4);
        return x % (4 * flapFrequency) < 4 * flapCount ? (int) Math.round(2 * thing * thing) : 0;
    }

    public static void renderAt(BufferBuilder bufferbuilder, Matrix4f matrix4f, int size, float angleOffset, float radius, float ellipsesRotation, float ellipticalWidth, Vector2f centerOffset, float height, float blocksPerSecond, boolean clockwise, Vector3f color, int renderTick, float partialTick) {
        double angle = Math.toRadians(angleOffset) + (clockwise ? -1 : 1) * (blocksPerSecond / radius) * (renderTick + partialTick) / 20;
        int frameId = getFrameId(size, blocksPerSecond, angleOffset, renderTick);


        for (int i = 0; i < 4; ++i) {
            boolean isInterior = i % 3 == 0; // if the vertex is facing towards the center or away
            boolean isFront = i >= 2; //if the vertex is at the front
            double sin = Math.sin(angle);
            double cos = Math.cos(angle);

            Vector2f dragonDirection = rotate(-ellipticalWidth * sin, (float) cos, ellipsesRotation);
            Vector2f pos = rotate(3 * size * (isInterior ? -1 : 1), 2 * size * (isFront ? 1 : -1), Math.atan2(dragonDirection.y, dragonDirection.x) - Math.PI/2);
            Vector2f offset = rotate(ellipticalWidth * radius * cos, radius * sin, ellipsesRotation);

            bufferbuilder.vertex(
                    matrix4f, //matrix
                    pos.x + offset.x + centerOffset.x,
                    isInterior ? height : height-radius/30, // height. exterior vertices are pushed down to reduce grazing angles
                    pos.y + offset.y + centerOffset.y
            ).uv(
                    isInterior ? 0f : 1f,
                    clockwise ^ isFront ? frameId/3f : (frameId+1)/3f //flips the dragon's uv if it's going in the opposite direction
            ).color(
                    color.x,
                    color.y,
                    color.z,
                    1f
            ).endVertex();
        }

        //bufferbuilder.vertex(matrix4f, (radius-3*size)*cos+2*size*sin, height, (radius-3*size)*sin-2*size*cos).uv(0f, clockwise ? uvy2 : uvy1).color(color.x, color.y, color.z, 1f).endVertex();
        //bufferbuilder.vertex(matrix4f, (radius+3*size)*cos+2*size*sin, height-radius/30, (radius+3*size)*sin-2*size*cos).uv(1f, clockwise ? uvy2 : uvy1).color(color.x, color.y, color.z, 1f).endVertex();
        //bufferbuilder.vertex(matrix4f, (radius+3*size)*cos-2*size*sin, height-radius/30, (radius+3*size)*sin+2*size*cos).uv(1f, clockwise ? uvy1 : uvy2).color(color.x, color.y, color.z, 1f).endVertex();
        //bufferbuilder.vertex(matrix4f, (radius-3*size)*cos-2*size*sin, height, (radius-3*size)*sin+2*size*cos).uv(0f, clockwise ? uvy1 : uvy2).color(color.x, color.y, color.z, 1f).endVertex();
    }

    // I should organize this better
    private static Vector2f rotate(double x, double y, double angle) {
        return new Vector2f(
                (float) (x * Math.cos(angle) - y * Math.sin(angle)),
                (float) (x * Math.sin(angle) + y * Math.cos(angle))
        );
    }

    private static int dragonCount(double distance) {
        distance = distance-dead_zone_radius;
        int out;

        if (distance > 4000) {
            out = (int) distance/2 - 2320;
        } else if (distance > 1000) {
            out = (int) distance/10 - 80;
        } else {
            out = (int) distance/50;
        }
        return Math.min(maxDragonCount, out);
    }

    private static List<Silhouette> generateSilhouettes(RandomSource randomSource) {
        List<Silhouette> list = new ArrayList<>();
        for (int i = 0; i < maxDragonCount; ++i) {
            list.add(new Silhouette(
                    randomSource.nextIntBetweenInclusive(2, 4),
                    randomSource.nextFloat() * 360,
                    randomSource.nextIntBetweenInclusive(30, 75),
                    randomSource.nextFloat() * 360,
                    randomSource.nextFloat() + 1,
                    new Vector2f(randomSource.nextFloat()*20-10, randomSource.nextFloat()*20-10),
                    randomSource.nextIntBetweenInclusive(20, 60),
                    randomSource.nextIntBetweenInclusive(10, 30),
                    randomSource.nextBoolean()
            ));
        }
        //list.sort(Comparator.comparing(o -> -o.height));
        return list;
    }

    private static class Silhouette {
        public int size;
        public float offsetAngle;
        public float radius;
        public float ellipsesRotation;
        public float ellipticalWidth;
        public Vector2f centerOffset;
        public float height;
        public float blocksPerSecond;
        public boolean clockwise;

        public Silhouette(int size, float offsetAngle, float radius, float ellipsesRotation, float ellipticalWidth, Vector2f centerOffset, float height, float blocksPerSecond, boolean clockwise) {
            this.size = size;
            this.offsetAngle = offsetAngle;
            this.radius = radius;
            this.ellipsesRotation = ellipsesRotation;
            this.ellipticalWidth = ellipticalWidth;
            this.centerOffset = centerOffset;
            this.height = height;
            this.blocksPerSecond = blocksPerSecond;
            this.clockwise = clockwise;
        }
    }
}

