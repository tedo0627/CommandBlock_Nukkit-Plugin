package tedo.CommandBlock;

public class BlockCommandBlockChain extends BlockCommandBlock{

	public BlockCommandBlockChain() {
		this(0);
	}

	public BlockCommandBlockChain(int meta) {
		super(meta);
	}

	@Override
	public int getId() {
		return 189;
	}

	@Override
	public String getName() {
		return "CommandBlockChain";
	}

	@Override
	public int getMode() {
		return 2;
	}

}
