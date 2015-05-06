# When to use this library #
If you want to efficiently store large collections of data in memory. This library can dramatically reduce Full GC times and reduce memory consumption as well.

When you have a data type which can be represented by an interface and you want a List for this type.

```
List<InterfaceType> list = new HugeArrayBuilder<InterfaceType>() {}.create();
```

The type needs to be described using an interface so its represention can be changed. (Using generated byte code)

The HugeArrayBuilder builds generated classes for the InterfaceType on demand.  The builder can be configured to change how the HugeArrayList is created.

A more complex example is
```
HugeArrayList<MutableTypes> hugeList = new HugeArrayBuilder<MutableTypes>() {{
        allocationSize = 1024*1024;
        classLoader = myClassLoader;
    }}.create();
```

# The source #
The best way to get the whole source, including tests is to use subversion.

```
svn co http://vanilla-java.googlecode.com/svn/trunk/collections/ vanilla-java-collections
cd vanilla-java-collections
mvn test
```

# How does the library differ from ArrayList #

  * Uses long for sizes and indecies.
  * Uses column based data making the per element overhead minimal and speed up scans over a single or small set of attributes. This reduces memory usages by 2x or more.
  * Stores attributes in direct memory as much as possible, reducing heap usage dramatically, 10x or more.
  * Allocates blocks of data for consistent add() times, rather than exponentially growing an underlying store (with exponentially increasing delays on a grow of capacity)

# Measures #
## Encoded and Primitive Fields ##
This uses the Sample Class, MutableTypes (see below). The class has 12 fields of various types and can be converted to primitives in different ways. By storing these primitives (or objects encoded as primitives), the time to perform GCs is almost eliminated.

![http://vanilla-java.googlecode.com/svn/wiki/images/huge-collection-gc-times.png](http://vanilla-java.googlecode.com/svn/wiki/images/huge-collection-gc-times.png)

| Object type | Average size | Average write+read time | Full GC Time for 250M elements |
|:------------|:-------------|:------------------------|:-------------------------------|
| List of JavaBean | 67 bytes| 123 ns| 23.3 seconds|
| HugeArrayList | 34 bytes| 129 ns| 0.0089 seconds|
| Proxied | 34 bytes| 1,420 ns| 0.0102 seconds|

The same data is stored in all cases.  The size of data in HugeArrayList is smaller as the data is stored in a more compact manner.

"Generated" uses ObjectWeb ASM to generate byte code.

"Proxied" uses the Proxy class and does not require any additional libraries.

## Plain Object Fields ##

Data Types with reference Objects which cannot be encoded as a primitive are also supported, however the benefit is reduced. If none of the fields can be encoded.


| Object type | Average size | Average write+read time | Full GC Time for 250M elements |
|:------------|:-------------|:------------------------|:-------------------------------|
| List of JavaBean | 36.8 bytes| 33 ns| 24.3 seconds|
| HugeArrayList | 16.2 bytes| 48 ns | 9.96 seconds|

The same data is stored in both cases.  The size of data in HugeArrayList is smaller as the data is stored in a more compact manner.

![http://vanilla-java.googlecode.com/svn/wiki/images/huge-collection-object-gc-times.png](http://vanilla-java.googlecode.com/svn/wiki/images/huge-collection-object-gc-times.png)

# Simple example #
```
interface MutableBoolean {
    public void setFlag(boolean b);
    public boolean getFlag();
}

// create a huge array of MutableBoolean
HugeArrayList<MutableBoolean> hugeList = new HugeArrayBuilder<MutableBoolean>() {}.create();
List<MutableBoolean> list = hugeList;
assertEquals(0, list.size());

final long length = 128 * 1000 * 1000 * 1000L; // uses 16 GB
hugeList.setSize(length);

assertEquals(Integer.MAX_VALUE, list.size());
assertEquals(length, hugeList.longSize());

boolean b = false;
long count = 0;
for (MutableBoolean mb : list) {
    mb.setFlag(b = !b);
    if ((int) count++ == 0)
        System.out.println("set " + count);
}

b = false;
count = 0;
for (MutableBoolean mb : list) {
    boolean b2 = mb.getFlag();
    boolean expected = b = !b;
    if (b2 != expected)
        assertEquals(expected, b2);
    if ((int) count++ == 0)
        System.out.println("get " + count);
}
```

# Sample Data Type #

```
public interface MutableTypes {
    public void setBoolean(boolean b);
    public boolean getBoolean();

    public void setBoolean2(Boolean b);
    public Boolean getBoolean2();

    public void setByte(byte b);
    public byte getByte();

    public void setByte2(Byte b);
    public Byte getByte2();

    public void setChar(char ch);
    public char getChar();

    public void setShort(short s);
    public short getShort();

    public void setInt(int i);
    public int getInt();

    public void setFloat(float f);
    public float getFloat();

    public void setLong(long l);
    public long getLong();

    public void setDouble(double d);
    public double getDouble();

    public void setElementType(ElementType elementType);
    public ElementType getElementType();

    public void setString(String text);
    public String getString();
}
```

## Creating an ArrayList ##

```
HugeArrayList<MutableTypes> hugeList = new HugeArrayBuilder<MutableTypes>() {}.create();
List<MutableTypes> list = hugeList;

hugeList.setSize(500*1000*1000); // increase the capacity to 500M

// give all the elements values.
int i = 0;
for (MutableTypes mb : list) {
    mb.setBoolean(i % 2 == 0);
    mb.setBoolean2(i % 3 == 0 ? null : i % 3 == 1);
    mb.setByte((byte) i);
    mb.setByte2(i % 31 == 0 ? null : (byte) i);
    mb.setChar((char) i);
    mb.setShort((short) i);
    mb.setInt(i);
    mb.setFloat(i);
    mb.setLong(i);
    mb.setDouble(i);
    mb.setElementType(elementTypes[i % elementTypes.length]);
    mb.setString(strings[i % strings.length]);
    i++;
}

// retrieve all the values.
for (MutableTypes mb : list) {
    boolean b1 = mb.getBoolean();
    Boolean b2 = mb.getBoolean2();
    byte b3 = mb.getByte();
    Byte b4 = mb.getByte2();
    char ch = mb.getChar();
    short s = mb.getShort();
    int i = mb.getInt();
    float f = mb.getFloat();
    long l = mb.getLong();
    double d = mb.getDouble();
    ElementType et = mb.getElementType();
    String text = mb.getString();
}

```