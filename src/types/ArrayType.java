package types;

public class ArrayType extends Type {
    
    private Type base;
    private int extent;
    
    public ArrayType(int extent, Type base)
    {
        this.extent = extent;
        this.base = base;
    }
    
    public int extent()
    {
        return extent;
    }
    
    public Type base()
    {
        return base;
    }
    
    @Override
    public String toString()
    {
        return "array[" + extent + "," + base + "]";
    }
    
    @Override
    public Type deref() {
        return new ArrayType(extent, base);
    }

    @Override
    public Type index(Type that) {
        if (!(that instanceof ArrayType))
            return super.index(that);
        return new ArrayType(extent, base);
    }
    
    @Override
    public Type assign(Type source) {
        if (!(source instanceof ArrayType))
            return super.assign(source);
        return new ArrayType(extent, base);
    }
    
    @Override
    public boolean equivalent(Type that)
    {
        if (that == null)
            return false;
        if (!(that instanceof IntType))
            return false;
        
        ArrayType aType = (ArrayType)that;
        return this.extent == aType.extent && base.equivalent(aType.base);
    }
}
