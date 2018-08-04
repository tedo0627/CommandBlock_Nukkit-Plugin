package tedo.CommandBlock;

public class BlockCommandBlockRepeating extends BlockCommandBlock{


	public BlockCommandBlockRepeating() {
		this(0);
	}

	public BlockCommandBlockRepeating(int meta) {
		super(meta);
	}

	@Override
	public int getId() {
		return 188;
	}

	@Override
	public String getName() {
		return "CommandBlockRepeating";
	}

	@Override
	public int getMode() {
		return 1;
	}

}
