package de.cubbossa.pathfinder.editmode.clientside;

import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.util.Quaternion4f;
import lombok.Getter;
import org.bukkit.Color;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;
import java.util.Objects;

@Getter
public class ClientDisplay extends ClientEntity implements Display {

  int interpolationDelay = 0;
  int interpolationDuration = 0;
  Transformation transformation = new Transformation(new Vector3f(), new AxisAngle4f(), new Vector3f(1, 1, 1), new AxisAngle4f());
  Billboard billboard = Billboard.FIXED;
  @Nullable Brightness brightness = null;
  float viewRange = 1;
  float shadowRadius = 0;
  float shadowStrength = 1;
  float displayWidth = 0;
  float displayHeight = 0;
  @Nullable Color glowColorOverride = null;

  public ClientDisplay(PlayerSpace playerSpace, int entityId, EntityType entityType) {
    super(playerSpace, entityId, entityType);
  }

  @Override
  public void setTransformation(@NotNull Transformation transformation) {
    if (this.transformation.equals(transformation)) {
      return;
    }
    this.transformation = transformation;
    metaChanged = true;
  }

  @Override
  public void setTransformationMatrix(@NotNull Matrix4f transformationMatrix) {
    transformationMatrix.getTranslation(this.transformation.getTranslation());
    transformationMatrix.getScale(this.transformation.getScale());
    transformationMatrix.getUnnormalizedRotation(this.transformation.getLeftRotation());
    metaChanged = true;
  }

  @Override
  public void setInterpolationDuration(int duration) {
    if (this.interpolationDuration == duration) {
      return;
    }
    this.interpolationDuration = duration;
    metaChanged = true;
  }

  @Override
  public void setViewRange(float range) {
    if (this.viewRange == range) {
      return;
    }
    this.viewRange = range;
    metaChanged = true;
  }

  @Override
  public void setShadowRadius(float radius) {
    if (this.shadowRadius == radius) {
      return;
    }
    this.shadowRadius = radius;
    metaChanged = true;
  }

  @Override
  public void setShadowStrength(float strength) {
    if (this.shadowStrength == strength) {
      return;
    }
    this.shadowStrength = strength;
    metaChanged = true;
  }

  @Override
  public void setDisplayWidth(float width) {
    if (this.displayWidth == width) {
      return;
    }
    this.displayWidth = width;
    metaChanged = true;
  }

  @Override
  public void setDisplayHeight(float height) {
    if (this.displayHeight == height) {
      return;
    }
    this.displayHeight = height;
    metaChanged = true;
  }

  @Override
  public void setInterpolationDelay(int ticks) {
    if (this.interpolationDelay == ticks) {
      return;
    }
    this.interpolationDelay = ticks;
    metaChanged = true;
  }

  @Override
  public void setBillboard(@NotNull Display.Billboard billboard) {
    if (this.billboard == billboard) {
      return;
    }
    this.billboard = billboard;
    metaChanged = true;
  }

  @Override
  public void setGlowColorOverride(@Nullable Color color) {
    if (Objects.equals(this.glowColorOverride, color)) {
      return;
    }
    this.glowColorOverride = color;
    metaChanged = true;
  }

  @Override
  public void setBrightness(@Nullable Display.Brightness brightness) {
    if (Objects.equals(this.brightness, brightness)) {
      return;
    }
    this.brightness = brightness;
    metaChanged = true;
  }

  @Override
  List<EntityData> metaData() {
    List<EntityData> data = super.metaData();
    if (interpolationDelay != 0) {
      data.add(new EntityData(8, EntityDataTypes.INT, interpolationDelay));
    }
    if (interpolationDuration != 0) {
      data.add(new EntityData(9, EntityDataTypes.INT, interpolationDuration));
    }
    if (!transformation.getTranslation().equals(new Vector3f(0, 0, 0))) {
      data.add(new EntityData(10, EntityDataTypes.VECTOR3F, convert(transformation.getTranslation())));
    }
    if (!transformation.getScale().equals(new Vector3f(1, 1, 1))) {
      data.add(new EntityData(11, EntityDataTypes.VECTOR3F, convert(transformation.getScale())));
    }
    data.add(new EntityData(12, EntityDataTypes.QUATERNION, convert(transformation.getLeftRotation())));
    data.add(new EntityData(13, EntityDataTypes.QUATERNION, convert(transformation.getRightRotation())));
    if (billboard != Billboard.FIXED) {
      data.add(new EntityData(14, EntityDataTypes.BYTE, (byte) billboard.ordinal()));
    }
    if (brightness != null) {
      data.add(new EntityData(15, EntityDataTypes.INT, brightness.getBlockLight() << 4 | brightness.getSkyLight() << 20));
    }
    if (viewRange != 1) {
      data.add(new EntityData(16, EntityDataTypes.FLOAT, viewRange));
    }
    if (shadowRadius != 0) {
      data.add(new EntityData(17, EntityDataTypes.FLOAT, shadowRadius));
    }
    if (shadowStrength != 1) {
      data.add(new EntityData(18, EntityDataTypes.FLOAT, shadowStrength));
    }
    if (displayWidth != 0) {
      data.add(new EntityData(19, EntityDataTypes.FLOAT, displayWidth));
    }
    if (displayHeight != 0) {
      data.add(new EntityData(20, EntityDataTypes.FLOAT, displayHeight));
    }
    if (glowColorOverride != null) {
      data.add(new EntityData(21, EntityDataTypes.INT, glowColorOverride.asRGB()));
    }
    return data;
  }

  Quaternion4f convert(Quaternionf quaternionf) {
    return new Quaternion4f(quaternionf.x, quaternionf.y, quaternionf.z, quaternionf.w);
  }

  com.github.retrooper.packetevents.util.Vector3f convert(Vector3f vector3f) {
    return new com.github.retrooper.packetevents.util.Vector3f(vector3f.x, vector3f.y, vector3f.z);
  }

  protected <T> T setMeta(T old, T val) {
    if (Objects.equals(old, val)) {
      return val;
    }
    metaChanged = true;
    return val;
  }
}
