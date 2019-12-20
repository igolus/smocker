function find (source, itemStart, itemEnd) {
	var indexStart = source.indexOf(itemStart);
	if (itemStart != -1) {
		var cut = source.substring(indexStart + itemStart.length);
		var indexEnd = cut.indexOf(itemEnd);
		if (indexEnd != -1) {
			return cut.substring(0, indexEnd);
		}

	}
	return null;

}



function atob(data) {
	// Web IDL requires DOMStrings to just be converted using ECMAScript
	// ToString, which in our case amounts to using a template literal.
	data = `${data}`;
	// "Remove all ASCII whitespace from data."
	data = data.replace(/[ \t\n\f\r]/g, "");
	// "If data's length divides by 4 leaving no remainder, then: if data ends
	// with one or two U+003D (=) code points, then remove them from data."
	if (data.length % 4 === 0) {
		data = data.replace(/==?$/, "");
	}
	// "If data's length divides by 4 leaving a remainder of 1, then return
	// failure."
	//
	// "If data contains a code point that is not one of
	//
	// U+002B (+)
	// U+002F (/)
	// ASCII alphanumeric
	//
	// then return failure."
	if (data.length % 4 === 1 || /[^+/0-9A-Za-z]/.test(data)) {
		return null;
	}
	// "Let output be an empty byte sequence."
	var output = "";
	// "Let buffer be an empty buffer that can have bits appended to it."
	//
	// We append bits via left-shift and or.  accumulatedBits is used to track
	// when we've gotten to 24 bits.
	var buffer = 0;
	var accumulatedBits = 0;
	// "Let position be a position variable for data, initially pointing at the
	// start of data."
	//
	// "While position does not point past the end of data:"
	for (var i = 0; i < data.length; i++) {
		// "Find the code point pointed to by position in the second column of
		// Table 1: The Base 64 Alphabet of RFC 4648. Let n be the number given in
		// the first cell of the same row.
		//
		// "Append to buffer the six bits corresponding to n, most significant bit
		// first."
		//
		// atobLookup() implements the table from RFC 4648.
		buffer <<= 6;
		buffer |= atobLookup(data[i]);
		accumulatedBits += 6;
		// "If buffer has accumulated 24 bits, interpret them as three 8-bit
		// big-endian numbers. Append three bytes with values equal to those
		// numbers to output, in the same order, and then empty buffer."
		if (accumulatedBits === 24) {
			output += String.fromCharCode((buffer & 0xff0000) >> 16);
			output += String.fromCharCode((buffer & 0xff00) >> 8);
			output += String.fromCharCode(buffer & 0xff);
			buffer = accumulatedBits = 0;
		}
		// "Advance position by 1."
	}
	// "If buffer is not empty, it contains either 12 or 18 bits. If it contains
	// 12 bits, then discard the last four and interpret the remaining eight as
	// an 8-bit big-endian number. If it contains 18 bits, then discard the last
	// two and interpret the remaining 16 as two 8-bit big-endian numbers. Append
	// the one or two bytes with values equal to those one or two numbers to
	// output, in the same order."
	if (accumulatedBits === 12) {
		buffer >>= 4;
		output += String.fromCharCode(buffer);
	} else if (accumulatedBits === 18) {
		buffer >>= 2;
		output += String.fromCharCode((buffer & 0xff00) >> 8);
		output += String.fromCharCode(buffer & 0xff);
	}
	// "Return output."
	return output;
}

function atobLookup(chr) {
	if (/[A-Z]/.test(chr)) {
		return chr.charCodeAt(0) - "A".charCodeAt(0);
	}
	if (/[a-z]/.test(chr)) {
		return chr.charCodeAt(0) - "a".charCodeAt(0) + 26;
	}
	if (/[0-9]/.test(chr)) {
		return chr.charCodeAt(0) - "0".charCodeAt(0) + 52;
	}
	if (chr === "+") {
		return 62;
	}
	if (chr === "/") {
		return 63;
	}
	// Throw exception; should not be hit in tests
	return undefined;
}


function find (source, itemStart, itemEnd) {
	var indexStart = source.indexOf(itemStart);
	//smockerLog("indexStart " + indexStart);
	//smockerLog("indexStartCheck" + itemStart == -1);

	if (indexStart != -1) {
		var cut = source.substring(indexStart + itemStart.length);
		var indexEnd = cut.indexOf(itemEnd);
		if (indexEnd != -1) {
			return cut.substring(0, indexEnd);
		}

	}
	return null;

}



function replace (source, itemStart, itemEnd, value) {
	var indexStart = source.indexOf(itemStart);
	if (itemStart != -1) {
		var cut = source.substring(indexStart + itemStart.length);
		var indexEnd = cut.indexOf(itemEnd);
		if (indexEnd != -1) {
			var indexEndGlobal = indexEnd + itemStart.length + indexStart + itemEnd.length;

			var ret = "";
			ret += source.substring(0, indexStart);
			ret += itemStart;
			ret += value;
			ret += itemEnd;
			ret += source.substring(indexEndGlobal);
			return ret;
		}

	}
	return null;

}


function escapeChar(c) {
	if (c == 8249) {
		return 139;
	}
	if (c == 8221) {
		return 148;
	}
	if (c == 8222) {
		return 132;
	}
	if (c == 8217) {
		return 146;
	}
	if (c == 732) {
		return 152;
	}
	return c;
}



function toIntArray(sourceStr) {
	var ret="";
	for (var i = 0; i < sourceStr.length; i++)
	{
		charCode = escapeChar(sourceStr.charCodeAt(i));
		var s = decimalToHex(charCode, null);
		if (s.length > 2) {
			smockerLog("place " + i);
			smockerLog("S2+ " + s);
			smockerLog("charCode " + sourceStr.charCodeAt(i));
		}
		ret+=s;
	}
	return ret;
}


function decimalToHex(d, padding) {
	var hex = Number(d).toString(16);
	padding = typeof (padding) === "undefined" || padding === null ? padding = 2 : padding;

	while (hex.length < padding) {
		hex = "0" + hex;
	}

	return hex;
}


function appendZero(nb) {
	var ret = "";
	for (var i = 0; i < nb; i++) {
		ret += String.fromCharCode(0);
	}
}


function Utf16ArrayToStr(array) {
	var ret = "";
	for (var i = 0; i < array.length; i++)
	{
		var value = array[i];
		ret += String.fromCharCode(value);
	}
	return ret;
}

function Utf8ArrayToStr(array) {
	var out, i, len, c;
	var char2, char3;

	out = "";
	len = array.length;
	i = 0;
	while (i < len) {
		c = array[i++];
		switch (c >> 4)
		{
		case 0: case 1: case 2: case 3: case 4: case 5: case 6: case 7:
			// 0xxxxxxx
			out += String.fromCharCode(c);
			break;
		case 12: case 13:
			// 110x xxxx   10xx xxxx
			char2 = array[i++];
			out += String.fromCharCode(((c & 0x1F) << 6) | (char2 & 0x3F));
			break;
		case 14:
			// 1110 xxxx  10xx xxxx  10xx xxxx
			char2 = array[i++];
			char3 = array[i++];
			out += String.fromCharCode(((c & 0x0F) << 12) |
					((char2 & 0x3F) << 6) |
					((char3 & 0x3F) << 0));
			break;
		}
	}    
	return out;
}



function toBytesInt(intValue) {
	var byteArray = [0, 0, 0, 0];
	byteArray[0] = intValue >> 24 & 0x00FF;
	byteArray[1] = intValue >> 16 & 0x00FF;
	byteArray[2] = intValue >> 8 & 0x00FF;
	byteArray[3] = intValue & 0x00FF;
	return byteArray;
}

function toBytesShort(intValue) {
	var byteArray = [0, 0];

	byteArray[0] = intValue >> 8 & 0x00FF;
	byteArray[1] = intValue & 0x00FF;

	return byteArray;
}


function decodeUTF8(bytes) {
	var i = 0, s = '';
	while (i < bytes.length) {
		var c = bytes[i++];
		if (c > 127) {
			if (c > 191 && c < 224) {
				if (i >= bytes.length)
					throw new Error('UTF-8 decode: incomplete 2-byte sequence');
				c = (c & 31) << 6 | bytes[i++] & 63;
			} else if (c > 223 && c < 240) {
				if (i + 1 >= bytes.length)
					throw new Error('UTF-8 decode: incomplete 3-byte sequence');
				c = (c & 15) << 12 | (bytes[i++] & 63) << 6 | bytes[i++] & 63;
			} else if (c > 239 && c < 248) {
				if (i + 2 >= bytes.length)
					throw new Error('UTF-8 decode: incomplete 4-byte sequence');
				c = (c & 7) << 18 | (bytes[i++] & 63) << 12 | (bytes[i++] & 63) << 6 | bytes[i++] & 63;
			}
		}
		if (c <= 0xffff) s += String.fromCharCode(c);
		else if (c <= 0x10ffff) {
			c -= 0x10000;
			s += String.fromCharCode(c >> 10 | 0xd800)
			s += String.fromCharCode(c & 0x3FF | 0xdc00)
		}
	}
	return s;
}


function fromUTF8Array(data) { // array of bytes
	var str = '',
	i;

	for (i = 0; i < data.length; i++) {
		var value = data[i];
		if (value < 0x80) {
			str += String.fromCharCode(value);
		} else if (value > 0xBF && value < 0xE0) {
			str += String.fromCharCode((value & 0x1F) << 6 | data[i + 1] & 0x3F);
			i += 1;
		} else if (value > 0xDF && value < 0xF0) {
			str += String.fromCharCode((value & 0x0F) << 12 | (data[i + 1] & 0x3F) << 6 | data[i + 2] & 0x3F);
			i += 2;
		} else {
			// surrogate pair
			var charCode = ((value & 0x07) << 18 | (data[i + 1] & 0x3F) << 12 | (data[i + 2] & 0x3F) << 6 | data[i + 3] & 0x3F) - 0x010000;

			str += String.fromCharCode(charCode >> 10 | 0xD800, charCode & 0x03FF | 0xDC00);
			i += 3;
		}
	}

	return str;
}



function smockerMock(providedOutput) {
	return smockerBtoa(toIntArray(providedOutput));
}

