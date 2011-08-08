package vanilla.java.collections.model;

/*
 * Copyright 2011 Peter Lawrey
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

public class Boolean2FieldModel implements FieldModel<Boolean> {
    private final String fieldName;
    private final int fieldNumber;
    private Method setter;
    private Method getter;

    public Boolean2FieldModel(String fieldName, int fieldNumber) {
        this.fieldName = fieldName;
        this.fieldNumber = fieldNumber;
    }

    @Override
    public Object arrayOfField(int size) {
        return newArrayOfField(size);
    }

    public static IntBuffer newArrayOfField(int size) {
        return ByteBuffer.allocateDirect(size / 4).order(ByteOrder.nativeOrder()).asIntBuffer();
    }

    @Override
    public Boolean getAllocation(Object[] arrays, int index) {
        IntBuffer array = (IntBuffer) arrays[fieldNumber];
        return get(array, index);
    }

    public static Boolean get(IntBuffer array, int index) {
        int value = array.get(index >>> 4) >> ((index & 15) << 1);
        switch (value & 3) {
            case 0:
                return false;
            case 1:
                return true;
            case 2:
                return null;
            default:
                throw new AssertionError();
        }
    }

    @Override
    public void setAllocation(Object[] arrays, int index, Boolean value) {
        IntBuffer array = (IntBuffer) arrays[fieldNumber];
        set(array, index, value);
    }

    public static void set(IntBuffer array, int index, Boolean value) {
        int value2 = value == null ? 2 : value ? 1 : 0;
        int byteIndex = index >>> 4;
        int index2 = (index & 15) << 1;
        int mask = ~(3 << index2);
        int value3 = (array.get(byteIndex) & mask) | (value2 << index2);
        array.put(byteIndex, value3);
    }

    @Override
    public void setter(Method setter) {
        this.setter = setter;
    }

    @Override
    public void getter(Method getter) {
        this.getter = getter;
    }

    @Override
    public int fieldNumber() {
        return fieldNumber;
    }

    @Override
    public Method setter() {
        return setter;
    }

    @Override
    public Method getter() {
        return getter;
    }

    @Override
    public Class<Boolean> type() {
        return (Class) boolean.class;
    }
}