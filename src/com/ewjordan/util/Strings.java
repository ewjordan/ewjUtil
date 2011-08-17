package com.ewjordan.util;

public class Strings {
	static public void println(Object ... objects) {
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<objects.length; ++i) {
			sb.append(objects[i].toString());
			if (i != objects.length - 1) {
				sb.append(", ");
			}
		}
		System.out.println(sb.toString());
	}
}
