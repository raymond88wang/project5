package types;

public class AddressType extends Type {
    
    private Type base;
    
    public AddressType(Type base)
    {
        this.base = base;
    }
    
    public Type base()
    {
        return base;
    }

    @Override
    public String toString()
    {
        return "Address(" + base + ")";
    }
    
    @Override
    public Type compare(Type that) {
        if (!(that instanceof AddressType))
            return super.compare(that);
        return new BoolType();
    }
    
    @Override
    public Type deref() {
        return new AddressType(base);
    }
    
    @Override
    public Type assign(Type source) {
        if (!(source instanceof AddressType))
            return super.assign(source);
        return new AddressType(base);
    }

    @Override
    public boolean equivalent(Type that) {
        if (that == null)
            return false;
        if (!(that instanceof AddressType))
            return false;
        
        AddressType aType = (AddressType)that;
        return this.base.equivalent(aType.base);
    }
}
