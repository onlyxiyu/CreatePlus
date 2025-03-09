package org.xiyu.yee.createplus.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.xiyu.yee.createplus.Createplus;
import org.xiyu.yee.createplus.features.CreativePlusFeature;

import java.util.ArrayList;
import java.util.List;

public class ModuleScreen extends Screen {
    private static final ResourceLocation BACKGROUND = new ResourceLocation(Createplus.MODID, "textures/gui/module_background.png");
    private static final int BUTTON_WIDTH = 100;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTONS_PER_ROW = 4;
    private static final int PADDING = 10;
    private static final int SLIDER_WIDTH = 150;
    private static final int SLIDER_HEIGHT = 20;
    
    private List<Button> moduleButtons = new ArrayList<>();
    private int scrollOffset = 0;
    private boolean isDragging = false;
    private int lastMouseY;
    private float hudOpacity;

    public ModuleScreen() {
        super(Component.translatable("createplus.modules.title"));
        this.hudOpacity = FeatureScreen.getOpacity();
    }

    @Override
    protected void init() {
        super.init();
        moduleButtons.clear();

        // 添加透明度滑块
        int sliderX = (width - SLIDER_WIDTH) / 2;
        int sliderY = 20;
        
        addRenderableWidget(new net.minecraft.client.gui.components.AbstractSliderButton(
            sliderX, sliderY, SLIDER_WIDTH, SLIDER_HEIGHT,
            Component.translatable("createplus.hud.opacity", 
                String.format("%.0f", hudOpacity * 100)),
            hudOpacity
        ) {
            @Override
            protected void updateMessage() {
                setMessage(Component.translatable("createplus.hud.opacity",
                    String.format("%.0f", value * 100)));
            }

            @Override
            protected void applyValue() {
                hudOpacity = (float) value;
                FeatureScreen.setOpacity(hudOpacity);
            }
        });

        // 原有的模块按钮布局
        int startX = (width - (BUTTON_WIDTH + PADDING) * BUTTONS_PER_ROW) / 2;
        int startY = 60; // 增加起始Y坐标,为滑块留出空间
        int x = startX;
        int y = startY;
        int count = 0;

        for (CreativePlusFeature feature : Createplus.FEATURE_MANAGER.getFeatures()) {
            Button button = Button.builder(
                Component.translatable(feature.getTranslationKey())
                    .append(Component.translatable(feature.isEnabled() ? 
                        "createplus.status.enabled" : "createplus.status.disabled")),
                btn -> {
                    if (feature.isEnabled()) {
                        Createplus.FEATURE_MANAGER.onDisable(feature);
                    } else {
                        Createplus.FEATURE_MANAGER.onEnable(feature);
                    }
                    btn.setMessage(Component.translatable(feature.getTranslationKey())
                        .append(Component.translatable(feature.isEnabled() ? 
                            "createplus.status.enabled" : "createplus.status.disabled")));
                }
            ).bounds(x, y + scrollOffset, BUTTON_WIDTH, BUTTON_HEIGHT).build();

            moduleButtons.add(button);
            addRenderableWidget(button);

            count++;
            if (count % BUTTONS_PER_ROW == 0) {
                x = startX;
                y += BUTTON_HEIGHT + PADDING;
            } else {
                x += BUTTON_WIDTH + PADDING;
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        
        // 渲染半透明背景
        graphics.fill(0, 0, width, height, 0x80000000);
        
        // 渲染标题
        graphics.drawCenteredString(font, title, width / 2, 10, 0xFFFFFF);

        // 更新按钮位置
        for (Button button : moduleButtons) {
            button.setY(button.getY() + scrollOffset);
        }

        super.render(graphics, mouseX, mouseY, partialTick);

        // 恢复按钮原始位置
        for (Button button : moduleButtons) {
            button.setY(button.getY() - scrollOffset);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        isDragging = true;
        lastMouseY = (int) mouseY;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        isDragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isDragging) {
            int deltaY = (int) mouseY - lastMouseY;
            scrollOffset += deltaY;
            
            // 限制滚动范围
            int maxScroll = Math.max(0, (moduleButtons.size() / BUTTONS_PER_ROW) * (BUTTON_HEIGHT + PADDING) - height + 60);
            scrollOffset = Math.max(-maxScroll, Math.min(0, scrollOffset));
            
            lastMouseY = (int) mouseY;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        scrollOffset += delta * 20;
        
        // 限制滚动范围
        int maxScroll = Math.max(0, (moduleButtons.size() / BUTTONS_PER_ROW) * (BUTTON_HEIGHT + PADDING) - height + 60);
        scrollOffset = Math.max(-maxScroll, Math.min(0, scrollOffset));
        
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // 将功能名称转换为翻译键
    private String getFeatureKey(String name) {
        return name.toLowerCase().replace(' ', '_');
    }
} 