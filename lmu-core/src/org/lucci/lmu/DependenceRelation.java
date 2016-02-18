package org.lucci.lmu;

public class DependenceRelation extends AssociationRelation{
	private static final String LABEL = "depends";
	public DependenceRelation(Entity tail, Entity head) {
		super(tail, head);
		super.setLabel(LABEL);
	}

	@Override
	public void setLabel(String label) {
		if(!super.getLabel().equals(LABEL)) {
			super.setLabel(LABEL);
		}
	}
}
