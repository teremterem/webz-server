/*
 * WebZ Server can serve web pages from various local and remote file sources.
 * Copyright (C) 2014-2015  Oleksandr Tereschenko <http://www.terems.org/>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.terems;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

public class StdErrOutLogger extends PrintStream {

	public static FileOutputStream install(File logFile) {

		FileOutputStream fileStream = null;

		PrintStream stdErr = System.err;
		PrintStream stdOut = System.out;
		try {
			fileStream = new FileOutputStream(logFile, true);
			// TODO think of some kind of log rotation logic

			System.setErr(new StdErrOutLogger(fileStream, stdErr));
			System.setOut(new StdErrOutLogger(fileStream, stdOut));

		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			stdErr.println("Failed to start writing logs to " + logFile.getAbsolutePath());
		}

		return fileStream;
	}

	// ~

	protected PrintStream stdStream;

	protected StdErrOutLogger(OutputStream fileStream, PrintStream stdStream) throws UnsupportedEncodingException {
		super(fileStream, false, "utf8");
		this.stdStream = stdStream;
	}

	@Override
	public void flush() {
		try {
			super.flush();
		} catch (Throwable th) {
			// ignore
		} finally {
			if (stdStream != null) {
				stdStream.flush();
			}
		}
	}

	@Override
	public void close() {
		try {
			super.close();
		} catch (Throwable th) {
			// ignore
		} finally {
			if (stdStream != null) {
				stdStream.close();
			}
		}
	}

	@Override
	public boolean checkError() {
		try {
			return super.checkError();
		} catch (Throwable th) {
			return true;
		} finally {
			if (stdStream != null) {
				stdStream.checkError();
			}
		}
	}

	@Override
	public void write(int b) {
		try {
			super.write(b);
		} catch (Throwable th) {
			// ignore
		} finally {
			if (stdStream != null) {
				stdStream.write(b);
			}
		}
	}

	@Override
	public void write(byte[] buf, int off, int len) {
		try {
			super.write(buf, off, len);
		} catch (Throwable th) {
			// ignore
		} finally {
			if (stdStream != null) {
				stdStream.write(buf, off, len);
			}
		}
	}

	@Override
	public void print(boolean b) {
		try {
			super.print(b);
		} catch (Throwable th) {
			// ignore
		} finally {
			if (stdStream != null) {
				stdStream.print(b);
			}
		}
	}

	@Override
	public void print(char c) {
		try {
			super.print(c);
		} catch (Throwable th) {
			// ignore
		} finally {
			if (stdStream != null) {
				stdStream.print(c);
			}
		}
	}

	@Override
	public void print(int i) {
		try {
			super.print(i);
		} catch (Throwable th) {
			// ignore
		} finally {
			if (stdStream != null) {
				stdStream.print(i);
			}
		}
	}

	@Override
	public void print(long l) {
		try {
			super.print(l);
		} catch (Throwable th) {
			// ignore
		} finally {
			if (stdStream != null) {
				stdStream.print(l);
			}
		}
	}

	@Override
	public void print(float f) {
		try {
			super.print(f);
		} catch (Throwable th) {
			// ignore
		} finally {
			if (stdStream != null) {
				stdStream.print(f);
			}
		}
	}

	@Override
	public void print(double d) {
		try {
			super.print(d);
		} catch (Throwable th) {
			// ignore
		} finally {
			if (stdStream != null) {
				stdStream.print(d);
			}
		}
	}

	@Override
	public void print(char[] s) {
		try {
			super.print(s);
		} catch (Throwable th) {
			// ignore
		} finally {
			if (stdStream != null) {
				stdStream.print(s);
			}
		}
	}

	@Override
	public void print(String s) {
		try {
			super.print(s);
		} catch (Throwable th) {
			// ignore
		} finally {
			if (stdStream != null) {
				stdStream.print(s);
			}
		}
	}

	@Override
	public void print(Object obj) {
		try {
			super.print(obj);
		} catch (Throwable th) {
			// ignore
		} finally {
			if (stdStream != null) {
				stdStream.print(obj);
			}
		}
	}

	@Override
	public void println() {
		try {
			super.println();
		} catch (Throwable th) {
			// ignore
		} finally {
			if (stdStream != null) {
				stdStream.println();
			}
		}
	}

	@Override
	public void println(boolean x) {
		try {
			super.println(x);
		} catch (Throwable th) {
			// ignore
		} finally {
			if (stdStream != null) {
				stdStream.println(x);
			}
		}
	}

	@Override
	public void println(char x) {
		try {
			super.println(x);
		} catch (Throwable th) {
			// ignore
		} finally {
			if (stdStream != null) {
				stdStream.println(x);
			}
		}
	}

	@Override
	public void println(int x) {
		try {
			super.println(x);
		} catch (Throwable th) {
			// ignore
		} finally {
			if (stdStream != null) {
				stdStream.println(x);
			}
		}
	}

	@Override
	public void println(long x) {
		try {
			super.println(x);
		} catch (Throwable th) {
			// ignore
		} finally {
			if (stdStream != null) {
				stdStream.println(x);
			}
		}
	}

	@Override
	public void println(float x) {
		try {
			super.println(x);
		} catch (Throwable th) {
			// ignore
		} finally {
			if (stdStream != null) {
				stdStream.println(x);
			}
		}
	}

	@Override
	public void println(double x) {
		try {
			super.println(x);
		} catch (Throwable th) {
			// ignore
		} finally {
			if (stdStream != null) {
				stdStream.println(x);
			}
		}
	}

	@Override
	public void println(char[] x) {
		try {
			super.println(x);
		} catch (Throwable th) {
			// ignore
		} finally {
			if (stdStream != null) {
				stdStream.println(x);
			}
		}
	}

	@Override
	public void println(String x) {
		try {
			super.println(x);
		} catch (Throwable th) {
			// ignore
		} finally {
			if (stdStream != null) {
				stdStream.println(x);
			}
		}
	}

	@Override
	public void println(Object x) {
		try {
			super.println(x);
		} catch (Throwable th) {
			// ignore
		} finally {
			if (stdStream != null) {
				stdStream.println(x);
			}
		}
	}

	@Override
	public PrintStream printf(String format, Object... args) {
		try {
			return super.printf(format, args);
		} catch (Throwable th) {
			return this;
		} finally {
			if (stdStream != null) {
				stdStream.printf(format, args);
			}
		}
	}

	@Override
	public PrintStream printf(Locale l, String format, Object... args) {
		try {
			return super.printf(l, format, args);
		} catch (Throwable th) {
			return this;
		} finally {
			if (stdStream != null) {
				stdStream.printf(l, format, args);
			}
		}
	}

	@Override
	public PrintStream format(String format, Object... args) {
		try {
			return super.format(format, args);
		} catch (Throwable th) {
			return this;
		} finally {
			if (stdStream != null) {
				stdStream.format(format, args);
			}
		}
	}

	@Override
	public PrintStream format(Locale l, String format, Object... args) {
		try {
			return super.format(l, format, args);
		} catch (Throwable th) {
			return this;
		} finally {
			if (stdStream != null) {
				stdStream.format(l, format, args);
			}
		}
	}

	@Override
	public PrintStream append(CharSequence csq) {
		try {
			return super.append(csq);
		} catch (Throwable th) {
			return this;
		} finally {
			if (stdStream != null) {
				stdStream.append(csq);
			}
		}
	}

	@Override
	public PrintStream append(CharSequence csq, int start, int end) {
		try {
			return super.append(csq, start, end);
		} catch (Throwable th) {
			return this;
		} finally {
			if (stdStream != null) {
				stdStream.append(csq, start, end);
			}
		}
	}

	@Override
	public PrintStream append(char c) {
		try {
			return super.append(c);
		} catch (Throwable th) {
			return this;
		} finally {
			if (stdStream != null) {
				stdStream.append(c);
			}
		}
	}

	@Override
	public void write(byte[] b) throws IOException {
		try {
			super.write(b);
		} catch (Throwable th) {
			// ignore
		} finally {
			if (stdStream != null) {
				stdStream.write(b);
			}
		}
	}

}
