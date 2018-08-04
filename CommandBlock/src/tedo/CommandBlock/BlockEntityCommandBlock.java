package tedo.CommandBlock;

import java.util.Map;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.blockentity.BlockEntitySpawnable;
import cn.nukkit.command.CommandSender;
import cn.nukkit.lang.TextContainer;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.BlockFace;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.ContainerOpenPacket;
import cn.nukkit.permission.PermissibleBase;
import cn.nukkit.permission.Permission;
import cn.nukkit.permission.PermissionAttachment;
import cn.nukkit.permission.PermissionAttachmentInfo;
import cn.nukkit.plugin.Plugin;

public class BlockEntityCommandBlock extends BlockEntitySpawnable implements CommandSender{

	public static final int TYPE_NORMAL = 0;
	public static final int TYPE_REPEATING = 1;
	public static final int TYPE_CHAIN = 2;

    private final PermissibleBase perm;

	public BlockEntityCommandBlock(FullChunk chunk, CompoundTag nbt) {
		super(chunk, nbt);

		if (!this.namedTag.contains("CustomName")) {
			this.namedTag.putString("CustomName", "");
		}
		if (!this.namedTag.contains("commandBlockMode")) {
			this.namedTag.putInt("commandBlockMode", 0);
		}
		if (!this.namedTag.contains("Command")) {
			this.namedTag.putString("Command", "");
		}
		if (!this.namedTag.contains("LastOutput")) {
			this.namedTag.putString("LastOutput", "");
		}
		if (!this.namedTag.contains("powered")) {
			this.namedTag.putBoolean("powered", false);
		}
		if (!this.namedTag.contains("auto")) {
			this.namedTag.putBoolean("auto", false);
		}
		if (!this.namedTag.contains("conditionalMode")) {
			this.namedTag.putBoolean("conditionalMode", false);
		}
		this.perm = new PermissibleBase(this);

		this.scheduleUpdate();
	}

	@Override
	public CompoundTag getSpawnCompound() {
		CompoundTag nbt = new CompoundTag()
				.putString("id", "CommandBlock")
				.putInt("x", this.getFloorX())
				.putInt("y", this.getFloorY())
				.putInt("z", this.getFloorZ())
				.putString("CustomName", this.namedTag.getString("CustomName"))
				.putInt("commandBlockMode", this.namedTag.getInt("commandBlockMode"))
				.putString("Command", this.namedTag.getString("Command"))
				.putString("LastOutput", this.namedTag.getString("LastOutput"))
				.putBoolean("powered", this.namedTag.getBoolean("powered"))
				.putBoolean("auto", this.namedTag.getBoolean("auto"))
				.putBoolean("conditionalMode", this.namedTag.getBoolean("conditionalMode"));
		return nbt;
	}

	@Override
    public void saveNBT() {
        this.namedTag.putString("id", "CommandBlock");
        this.namedTag.putInt("x", (int) this.getX());
        this.namedTag.putInt("y", (int) this.getY());
        this.namedTag.putInt("z", (int) this.getZ());
        this.namedTag.putBoolean("isMovable", this.movable);
    }

	@Override
	public boolean isBlockEntityValid() {
		int blockID = getBlock().getId();
		switch (blockID) {
			case 137:
				return this.isNormal();

			case 188:
				return this.isRepeating();

			case 189:
				return this.isChain();

			default:
				return false;
		}
	}

	@Override
	public boolean onUpdate() {
		if (this.closed) {
			return false;
		}
		if (!this.isRepeating()) {
			return true;
		}
		if (this.getAuto()) {
			this.useCommand();
		} else {
			if (this.getPower()) {
				this.useCommand();
			}
		}
		return true;
	}



    public boolean isNormal() {
    	return this.getMode() == TYPE_NORMAL;
    }

    public boolean isRepeating() {
    	return this.getMode() == TYPE_REPEATING;
    }

    public boolean isChain() {
    	return this.getMode() == TYPE_CHAIN;
    }



	public void show(Player player) {
		ContainerOpenPacket pk = new ContainerOpenPacket();
		pk.type = 16;
		pk.windowId = 64;
		pk.x = this.getFloorX();
		pk.y = this.getFloorY();
		pk.z = this.getFloorZ();
		player.dataPacket(pk);
	}

	public void updatePower(boolean power) {
		if (this.getPower() == power) {
			return;
		}
		this.setPower(power);
		if (!this.isNormal()) {
			return;
		}
		if (power) {
			if (this.getAuto()) {
				return;
			}
			this.useCommand();
		}
	}

	public void chainUpdate() {
		if (this.getAuto()) {
			this.useCommand();
		} else {
			if (this.getPower()) {
				this.useCommand();
			}
		}
    }

	public void useCommand() {
		boolean check = this.getServer().dispatchCommand(this, this.getCommand());
		Block block = getBlock();
		Block commandBlock = block.getSide(BlockFace.fromIndex(block.getDamage()));
		if (commandBlock instanceof BlockCommandBlockChain) {
			if (block.getDamage() != commandBlock.getDamage()) {
				return;
			}
			BlockEntityCommandBlock blockEntity = ((BlockCommandBlockChain) commandBlock).getBlockEntity();
			if (blockEntity == null) {
				return;
			}
			if (blockEntity.getConditions()) {
				if (!check) {
					return;
				}
			}
			blockEntity.chainUpdate();
		}
	}



    public void setName(String name) {
    	this.namedTag.putString("CustomName", name);
    }

    public String getName() {
    	return this.namedTag.getString("CustomName").equals("") ? "CommandBlock" : this.namedTag.getString("CustomName");
    }



    public void setMode(int mode) {
    	this.namedTag.putInt("commandBlockMode", mode);
    }

    public int getMode() {
    	return this.namedTag.getInt("commandBlockMode");
    }



    public void setCommand(String command) {
    	this.namedTag.putString("Command", command);
    }

    public String getCommand() {
    	return this.namedTag.getString("Command");
    }



    public void setLastOutPut(String log) {
    	this.namedTag.putString("LastOutput", log);
    }

    public String getLastOutPut() {
    	return this.namedTag.getString("LastOutput");
    }



    public void setPower(boolean power) {
    	this.namedTag.putBoolean("powered", power);
    }

    public boolean getPower() {
    	return this.namedTag.getBoolean("powered");
    }



    public void setAuto(boolean auto) {
    	this.namedTag.putBoolean("auto", auto);
    }

    public boolean getAuto() {
    	return this.namedTag.getBoolean("auto");
    }



    public void setConditions(boolean condition) {
    	this.namedTag.putBoolean("conditionalMode", condition);
    }

    public boolean getConditions() {
    	return this.namedTag.getBoolean("conditionalMode");
    }




    @Override
    public boolean isPermissionSet(String name) {
        return this.perm.isPermissionSet(name);
    }

    @Override
    public boolean isPermissionSet(Permission permission) {
        return this.perm.isPermissionSet(permission);
    }

    @Override
    public boolean hasPermission(String name) {
        return this.perm.hasPermission(name);
        }

    @Override
    public boolean hasPermission(Permission permission) {
        return this.perm.hasPermission(name);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin) {
        return this.perm.addAttachment(plugin);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name) {
        return this.perm.addAttachment(plugin, name);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, Boolean value) {
        return this.perm.addAttachment(plugin, name, value);
    }

    @Override
    public void removeAttachment(PermissionAttachment attachment) {
        this.perm.removeAttachment(attachment);
    }

    @Override
    public void recalculatePermissions() {
        this.perm.recalculatePermissions();
    }

    @Override
    public Map<String, PermissionAttachmentInfo> getEffectivePermissions() {
        return this.perm.getEffectivePermissions();
    }

    @Override
    public boolean isOp() {
        return true;
    }

    @Override
    public void setOp(boolean value) {
    }

    @Override
    public void sendMessage(String message) {
        this.setLastOutPut(message);
    }

    @Override
    public void sendMessage(TextContainer message) {
        this.setLastOutPut(this.getServer().getLanguage().translate(message));
    }

    @Override
    public Server getServer() {
        return Server.getInstance();
    }

    @Override
    public boolean isPlayer() {
        return false;
    }
}
