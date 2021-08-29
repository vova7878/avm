package com.v7878.avm.bytecode;

public class Init {

    private static boolean inited;

    public static void init() {
        if (!inited) {
            inited = true;

            AddDouble.init();
            AddFloat.init();
            AddInt8.init();
            AddInt16.init();
            AddInt32.init();
            AddInt64.init();
            AddInt128.init();

            AndInt8.init();
            AndInt16.init();
            AndInt32.init();
            AndInt64.init();
            AndInt128.init();

            CmpDouble.init();
            CmpFloat.init();
            CmpInt8.init();
            CmpInt16.init();
            CmpInt32.init();
            CmpInt64.init();
            CmpInt128.init();
            CmpUInt8.init();
            CmpUInt16.init();
            CmpUInt32.init();
            CmpUInt64.init();
            CmpUInt128.init();

            CmpzDouble.init();
            CmpzFloat.init();
            CmpzInt8.init();
            CmpzInt16.init();
            CmpzInt32.init();
            CmpzInt64.init();
            CmpzInt128.init();
            CmpzUInt8.init();
            CmpzUInt16.init();
            CmpzUInt32.init();
            CmpzUInt64.init();
            CmpzUInt128.init();

            Const8.init();
            Const16.init();
            Const32.init();
            Const64.init();
            Const128.init();

            DivDouble.init();
            DivFloat.init();
            DivInt8.init();
            DivInt16.init();
            DivInt32.init();
            DivInt64.init();
            DivInt128.init();
            DivUInt8.init();
            DivUInt16.init();
            DivUInt32.init();
            DivUInt64.init();
            DivUInt128.init();

            IfEqz.init();
            IfGez.init();
            IfGtz.init();
            IfLez.init();
            IfLtz.init();
            IfNez.init();

            Move8.init();
            Move16.init();
            Move32.init();
            Move64.init();
            Move128.init();
            MoveA.init();

            MulDouble.init();
            MulFloat.init();
            MulInt8.init();
            MulInt16.init();
            MulInt32.init();
            MulInt64.init();
            MulInt128.init();

            NegDouble.init();
            NegFloat.init();
            NegInt8.init();
            NegInt16.init();
            NegInt32.init();
            NegInt64.init();
            NegInt128.init();

            NotInt8.init();
            NotInt16.init();
            NotInt32.init();
            NotInt64.init();
            NotInt128.init();

            OrInt8.init();
            OrInt16.init();
            OrInt32.init();
            OrInt64.init();
            OrInt128.init();

            RemDouble.init();
            RemFloat.init();
            RemInt8.init();
            RemInt16.init();
            RemInt32.init();
            RemInt64.init();
            RemInt128.init();
            RemUInt8.init();
            RemUInt16.init();
            RemUInt32.init();
            RemUInt64.init();
            RemUInt128.init();

            ShlInt8.init();
            ShlInt16.init();
            ShlInt32.init();
            ShlInt64.init();
            ShlInt128.init();

            ShrInt8.init();
            ShrInt16.init();
            ShrInt32.init();
            ShrInt64.init();
            ShrInt128.init();

            SubDouble.init();
            SubFloat.init();
            SubInt8.init();
            SubInt16.init();
            SubInt32.init();
            SubInt64.init();
            SubInt128.init();

            UShrInt8.init();
            UShrInt16.init();
            UShrInt32.init();
            UShrInt64.init();
            UShrInt128.init();

            XorInt8.init();
            XorInt16.init();
            XorInt32.init();
            XorInt64.init();
            XorInt128.init();
            
            DoubleToInt32.init();
            DoubleToInt64.init();
            Int8ToInt16.init();
            Int8ToInt32.init();
            Int8ToInt64.init();
            Int8ToInt128.init();
            Int16ToInt32.init();
            Int16ToInt64.init();
            Int16ToInt128.init();
            Int32ToDouble.init();
            Int32ToFloat.init();
            Int32ToInt64.init();
            Int32ToInt128.init();
            Int64ToInt128.init();
            Int64ToFloat.init();
            Int64ToDouble.init();
            Int128ToFloat.init();
            Int128ToDouble.init();

            Nop.init();
            Return.init();
            GetThis.init();
            Goto.init();
            FindNode.init();
            Invoke.init();
            SizeOf.init();
        }
    }
}
