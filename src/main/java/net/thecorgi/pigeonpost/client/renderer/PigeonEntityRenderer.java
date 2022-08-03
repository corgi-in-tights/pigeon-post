package net.thecorgi.pigeonpost.client.renderer;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.thecorgi.pigeonpost.client.model.PigeonEntityModel;
import net.thecorgi.pigeonpost.common.entity.PigeonEntity;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

import java.util.Optional;

public class PigeonEntityRenderer extends GeoEntityRenderer<PigeonEntity> {
    public PigeonEntityRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new PigeonEntityModel());
    }

    @Override
    public void render(GeoModel model, PigeonEntity animatable, float partialTicks, RenderLayer type,
                       MatrixStack matrixStackIn, VertexConsumerProvider renderTypeBuffer, VertexConsumer vertexBuilder,
                       int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {

        model.getBone("Bag").ifPresent(geoBone -> geoBone.setHidden(!animatable.isTamed()));
        model.getBone("sunglasses").ifPresent(geoBone -> geoBone.setHidden(!animatable.isCool()));

        super.render(model, animatable, partialTicks, type, matrixStackIn, renderTypeBuffer, vertexBuilder,
                packedLightIn, packedOverlayIn, red, green, blue, alpha);
    }
}
