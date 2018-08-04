package tedo.CommandBlock;

import java.lang.reflect.Constructor;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockIce;
import cn.nukkit.block.BlockLiquid;
import cn.nukkit.block.BlockUnknown;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.item.Item;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.protocol.CommandBlockUpdatePacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.plugin.PluginBase;

public class Main extends PluginBase implements Listener {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void onEnable() {

		this.getServer().getPluginManager().registerEvents(this, this);

		this.getServer().getNetwork().registerPacket(ProtocolInfo.COMMAND_BLOCK_UPDATE_PACKET, CommandBlockUpdatePacket.class);

		BlockEntity.registerBlockEntity("CommandBlock", BlockEntityCommandBlock.class);

		Block.list[137] = BlockCommandBlock.class;
		Block.list[188] = BlockCommandBlockRepeating.class;
		Block.list[189] = BlockCommandBlockChain.class;

		Item.list[137] = BlockCommandBlock.class;
		Item.list[188] = BlockCommandBlockRepeating.class;
		Item.list[189] = BlockCommandBlockChain.class;

		int[] a = new int[]{137, 188, 189};

		for (int id : a) {
			Class c = Block.list[id];
			if (c != null) {
				Block block;
				try {
					block = (Block) c.newInstance();
					try {
						Constructor constructor = c.getDeclaredConstructor(int.class);
						constructor.setAccessible(true);
						for (int data = 0; data < 16; ++data) {
							Block.fullList[(id << 4) | data] = (Block) constructor.newInstance(data);
						}
						Block.hasMeta[id] = true;
					} catch (NoSuchMethodException ignore) {
						for (int data = 0; data < 16; ++data) {
							Block.fullList[(id << 4) | data] = block;
						}
					}
				} catch (Exception e) {
					Server.getInstance().getLogger().error("Error while registering " + c.getName(), e);
					for (int data = 0; data < 16; ++data) {
						Block.fullList[(id << 4) | data] = new BlockUnknown(id, data);
					}
					return;
				}

				Block.solid[id] = block.isSolid();
				Block.transparent[id] = block.isTransparent();
				Block.hardness[id] = block.getHardness();
				Block.light[id] = block.getLightLevel();

				if (block.isSolid()) {
					if (block.isTransparent()) {
						if (block instanceof BlockLiquid || block instanceof BlockIce) {
							Block.lightFilter[id] = 2;
						} else {
							Block.lightFilter[id] = 1;
						}
					} else {
						Block.lightFilter[id] = 15;
					}
				} else {
					Block.lightFilter[id] = 1;
				}
			} else {
				Block.lightFilter[id] = 1;
				for (int data = 0; data < 16; ++data) {
					Block.fullList[(id << 4) | data] = new BlockUnknown(id, data);
				}
			}
		}
	}

	@EventHandler
	public void onDataPacketReceive(DataPacketReceiveEvent event) {
		if (!(event.getPacket() instanceof CommandBlockUpdatePacket)) {
			return;
		}
		Player player = event.getPlayer();
		if (!(player.isOp() && player.isCreative())) {
			return;
		}
		CommandBlockUpdatePacket pk = (CommandBlockUpdatePacket) event.getPacket();
		if (pk.isBlock) {
			Vector3 pos = new Vector3(pk.x, pk.y, pk.z);
			Block block = player.level.getBlock(pos);
			if (block instanceof BlockCommandBlock) {
				BlockEntityCommandBlock blockEntity = ((BlockCommandBlock) block).getBlockEntity();
				if (blockEntity == null) {
					return;
				}
				Block place = Block.get(137);
				switch (pk.commandBlockMode) {
					case 0:
						place = Block.get(137);
						place.setDamage(block.getDamage());
						break;
					case 1:
						place = Block.get(188);
						place.setDamage(block.getDamage());
						break;
					case 2:
						place = Block.get(189);
						place.setDamage(block.getDamage());
						break;
				}
				if (pk.isConditional) {
					if (place.getDamage() < 8) {
						place.setDamage(place.getDamage() + 8);
					}
				} else {
					if (place.getDamage() > 8) {
						place.setDamage(place.getDamage() - 8);
					}
				}
				player.level.setBlock(pos, place, false, false);
				blockEntity.setName(pk.name);
				blockEntity.setMode(pk.commandBlockMode);
				blockEntity.setCommand(pk.command);
				blockEntity.setLastOutPut(pk.lastOutput);
				blockEntity.setAuto(!pk.isRedstoneMode);
				blockEntity.setConditions(pk.isConditional);
				blockEntity.spawnToAll();
			}
		} else {
			//MinercartCommandBlock
		}
	}
}
