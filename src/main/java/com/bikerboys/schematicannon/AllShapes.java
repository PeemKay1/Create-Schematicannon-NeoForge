package com.bikerboys.schematicannon;

import static net.minecraft.core.Direction.SOUTH;

import java.util.EnumMap;
import java.util.Map;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class AllShapes {


	public static final Map<Direction, VoxelShape> CLIPBOARD_FLOOR =
		shape(3, 0, 1, 13, 1, 15).forHorizontal(SOUTH);
	public static final Map<Direction, VoxelShape> CLIPBOARD_CEILING =
		shape(3, 15, 1, 13, 16, 15).forHorizontal(SOUTH);
	public static final Map<Direction, VoxelShape> CLIPBOARD_WALL =
		shape(3, 1, 0, 13, 15, 1).forHorizontal(SOUTH);


	public static final VoxelShape TABLE_POLE_SHAPE = shape(4, 0, 4, 12, 2, 12).add(5, 2, 5, 11, 14, 11)
			.build();
	public static final VoxelShape SCHEMATICANNON_SHAPE = shape(1, 0, 1, 15, 8, 15).add(0.5, 8, 0.5, 15.5, 11, 15.5)
			.build();


	// More Shapers
	public static final Map<Direction, VoxelShape>

		SCHEMATICS_TABLE = shape(4, 0, 4, 12, 12, 12).add(0, 11, 2, 16, 14, 14)
			.forDirectional(SOUTH)


	;

	private static Builder shape(VoxelShape shape) {
		return new Builder(shape);
	}

	private static Builder shape(double x1, double y1, double z1, double x2, double y2, double z2) {
		return shape(cuboid(x1, y1, z1, x2, y2, z2));
	}

	private static VoxelShape cuboid(double x1, double y1, double z1, double x2, double y2, double z2) {
		return Block.box(x1, y1, z1, x2, y2, z2);
	}

	public static class Builder {

		private VoxelShape shape;

		public Builder(VoxelShape shape) {
			this.shape = shape;
		}

		public Builder add(VoxelShape shape) {
			this.shape = Shapes.or(this.shape, shape);
			return this;
		}

		public Builder add(double x1, double y1, double z1, double x2, double y2, double z2) {
			return add(cuboid(x1, y1, z1, x2, y2, z2));
		}

		public Builder erase(double x1, double y1, double z1, double x2, double y2, double z2) {
			this.shape = Shapes.join(shape, cuboid(x1, y1, z1, x2, y2, z2), BooleanOp.ONLY_FIRST);
			return this;
		}

		public VoxelShape build() {
			return shape;
		}

		public Map<Direction, VoxelShape> forHorizontal(Direction sourceDirection) {
			if (!sourceDirection.getAxis().isHorizontal())
				throw new IllegalArgumentException("Source direction must be horizontal");

			Map<Direction, VoxelShape> northOriented = Shapes.rotateHorizontal(shape);
			EnumMap<Direction, VoxelShape> result = new EnumMap<>(Direction.class);
			int offset = sourceDirection.get2DDataValue() - Direction.NORTH.get2DDataValue();
			for (Direction target : Direction.Plane.HORIZONTAL) {
				int sourceIndex = Math.floorMod(target.get2DDataValue() - offset, 4);
				result.put(target, northOriented.get(Direction.from2DDataValue(sourceIndex)));
			}
			return result;
		}

		public Map<Direction, VoxelShape> forDirectional(Direction sourceDirection) {
			return forHorizontal(sourceDirection);
		}

	}

}
