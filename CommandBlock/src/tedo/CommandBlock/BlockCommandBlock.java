package tedo.CommandBlock;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockSolidMeta;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.math.BlockFace;
import cn.nukkit.nbt.tag.CompoundTag;

public class BlockCommandBlock extends BlockSolidMeta {

	protected int meta;

	public BlockCommandBlock() {
		this(0);
	}

	public BlockCommandBlock(int meta) {
		super(meta);
	}

	@Override
	public int getId() {
		return 137;
	}

	@Override
	public String getName() {
		return "CommandBlock";
	}

	@Override
	public boolean canBeActivated() {
		return true;
	}

	public BlockEntityCommandBlock getBlockEntity() {
		BlockEntity blockEntity = this.level.getBlockEntity(this);
		if (blockEntity instanceof BlockEntityCommandBlock) {
			return (BlockEntityCommandBlock) blockEntity;
		} else {
			return null;
		}
	}

    @Override
    public boolean place(Item item, Block block, Block target, BlockFace face, double fx, double fy, double fz, Player player) {
        if (!(player.isOp() && player.isCreative())) {
            return false;
        }
        int f = 0;
        if (player instanceof Player) {
            double pitch = player.getPitch();
            if (Math.abs(pitch) >= 45) {
                if (pitch < 0) {
                    f = 4;
                } else {
                    f = 5;
                }
            } else {
                f = player.getDirection().getHorizontalIndex();
            }
        }
        int[] faces = new int[]{2, 5, 3, 4, 0, 1};
        this.setDamage(faces[f]);
        this.getLevel().setBlock(block, this, true, true);
        CompoundTag nbt = new CompoundTag()
                .putString("id", "CommandBlock")
                .putInt("x", this.getFloorX())
                .putInt("y", this.getFloorY())
                .putInt("z", this.getFloorZ())
                .putInt("commandBlockMode", this.getMode());

        new BlockEntityCommandBlock(this.level.getChunk(this.getFloorX() >> 4, this.getFloorZ() >> 4), nbt);

        return true;
    }

    @Override
    public boolean onActivate(Item item, Player player) {
        if (!(player.isOp() && player.isCreative())) {
            return false;
        }
        BlockEntityCommandBlock blockEntity = this.getBlockEntity();
        if (blockEntity == null) {
            CompoundTag nbt = new CompoundTag()
                    .putString("id", "CommandBlock")
                    .putInt("x", this.getFloorX())
                    .putInt("y", this.getFloorY())
                    .putInt("z", this.getFloorZ())
                    .putInt("commandBlockMode", this.getMode());
            blockEntity = new BlockEntityCommandBlock(this.level.getChunk(this.getFloorX() >> 4, this.getFloorZ() >> 4), nbt);
        }
        this.level.setBlock(this, this);
        blockEntity.spawnTo(player);
        blockEntity.show(player);
        return true;
    }

    @Override
    public int onUpdate(int type) {
        if (type == Level.BLOCK_UPDATE_NORMAL || type == Level.BLOCK_UPDATE_REDSTONE) {
            BlockEntityCommandBlock blockEntity = this.getBlockEntity();
            if (blockEntity == null) {
                return 0;
            }
            blockEntity.updatePower(this.level.isBlockPowered(this));
            return 1;
        }
        return 0;
    }

    public int getMode() {
        return 0;
    }

}
