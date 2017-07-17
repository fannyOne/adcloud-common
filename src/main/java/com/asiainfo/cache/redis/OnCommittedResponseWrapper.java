package com.asiainfo.cache.redis;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

abstract class OnCommittedResponseWrapper extends HttpServletResponseWrapper {
    private final Log logger = LogFactory.getLog(getClass());

    private boolean disableOnCommitted;

    private long contentLength;

    private long contentWritten;

    OnCommittedResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    @Override
    public void addHeader(String name, String value) {
        if ("Content-Length".equalsIgnoreCase(name)) {
            setContentLength(Long.parseLong(value));
        }
        super.addHeader(name, value);
    }

    @Override
    public void setContentLength(int len) {
        setContentLength((long) len);
        super.setContentLength(len);
    }

    private void setContentLength(long len) {
        this.contentLength = len;
        checkContentLength(0);
    }

    public void disableOnResponseCommitted() {
        this.disableOnCommitted = true;
    }

    protected abstract void onResponseCommitted();

    @Override
    public final void sendError(int sc) throws IOException {
        doOnResponseCommitted();
        super.sendError(sc);
    }

    @Override
    public final void sendError(int sc, String msg) throws IOException {
        doOnResponseCommitted();
        super.sendError(sc, msg);
    }

    @Override
    public final void sendRedirect(String location) throws IOException {
        doOnResponseCommitted();
        super.sendRedirect(location);
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return null;//new SaveContextServletOutputStream(super.getOutputStream());
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return new SaveContextPrintWriter(super.getWriter());
    }

    @Override
    public void flushBuffer() throws IOException {
        doOnResponseCommitted();
        super.flushBuffer();
    }

    private void trackContentLength(boolean content) {
        checkContentLength(content ? 4 : 5); // TODO Localization
    }

    private void trackContentLength() {
        checkContentLength(1);
    }

    private void trackContentLength(Object content) {
        trackContentLength(String.valueOf(content));
    }

    private void trackContentLength(byte[] content) {
        checkContentLength(content == null ? 0 : content.length);
    }

    private void trackContentLength(char[] content) {
        checkContentLength(content == null ? 0 : content.length);
    }

    private void trackContentLength(int content) {
        trackContentLength(String.valueOf(content));
    }

    private void trackContentLength(float content) {
        trackContentLength(String.valueOf(content));
    }

    private void trackContentLength(double content) {
        trackContentLength(String.valueOf(content));
    }

    private void trackContentLengthLn() {
        trackContentLength("\r\n");
    }

    private void trackContentLength(String content) {
        checkContentLength(content.length());
    }

    private void checkContentLength(long contentLengthToWrite) {
        this.contentWritten += contentLengthToWrite;
        boolean isBodyFullyWritten = this.contentLength > 0
            && this.contentWritten >= this.contentLength;
        int bufferSize = getBufferSize();
        boolean requiresFlush = bufferSize > 0 && this.contentWritten >= bufferSize;
        if (isBodyFullyWritten || requiresFlush) {
            doOnResponseCommitted();
        }
    }

    private void doOnResponseCommitted() {
        if (!this.disableOnCommitted) {
            onResponseCommitted();
            disableOnResponseCommitted();
        } else if (this.logger.isDebugEnabled()) {
            this.logger.debug("Skip invoking on");
        }
    }

    private class SaveContextPrintWriter extends PrintWriter {
        private final PrintWriter delegate;

        SaveContextPrintWriter(PrintWriter delegate) {
            super(delegate);
            this.delegate = delegate;
        }

        public void flush() {
            doOnResponseCommitted();
            this.delegate.flush();
        }

        public void close() {
            doOnResponseCommitted();
            this.delegate.close();
        }

        public int hashCode() {
            return this.delegate.hashCode();
        }

        public boolean equals(Object obj) {
            return this.delegate.equals(obj);
        }

        public String toString() {
            return getClass().getName() + "[delegate=" + this.delegate.toString() + "]";
        }

        public boolean checkError() {
            return this.delegate.checkError();
        }

        public void write(int c) {
            trackContentLength(c);
            this.delegate.write(c);
        }

        public void write(char[] buf, int off, int len) {
            checkContentLength(len);
            this.delegate.write(buf, off, len);
        }

        public void write(char[] buf) {
            trackContentLength(buf);
            this.delegate.write(buf);
        }

        public void write(String s, int off, int len) {
            checkContentLength(len);
            this.delegate.write(s, off, len);
        }

        public void write(String s) {
            trackContentLength(s);
            this.delegate.write(s);
        }

        public void print(boolean b) {
            trackContentLength(b);
            this.delegate.print(b);
        }

        public void print(char c) {
            trackContentLength();
            this.delegate.print(c);
        }

        public void print(int i) {
            trackContentLength(i);
            this.delegate.print(i);
        }

        public void print(long l) {
            trackContentLength(l);
            this.delegate.print(l);
        }

        public void print(float f) {
            trackContentLength(f);
            this.delegate.print(f);
        }

        public void print(double d) {
            trackContentLength(d);
            this.delegate.print(d);
        }

        public void print(char[] s) {
            trackContentLength(s);
            this.delegate.print(s);
        }

        public void print(String s) {
            trackContentLength(s);
            this.delegate.print(s);
        }

        public void print(Object obj) {
            trackContentLength(obj);
            this.delegate.print(obj);
        }

        public void println() {
            trackContentLengthLn();
            this.delegate.println();
        }

        public void println(boolean x) {
            trackContentLength(x);
            trackContentLengthLn();
            this.delegate.println(x);
        }

        public void println(char x) {
            trackContentLength();
            trackContentLengthLn();
            this.delegate.println(x);
        }

        public void println(int x) {
            trackContentLength(x);
            trackContentLengthLn();
            this.delegate.println(x);
        }

        public void println(long x) {
            trackContentLength(x);
            trackContentLengthLn();
            this.delegate.println(x);
        }

        public void println(float x) {
            trackContentLength(x);
            trackContentLengthLn();
            this.delegate.println(x);
        }

        public void println(double x) {
            trackContentLength(x);
            trackContentLengthLn();
            this.delegate.println(x);
        }

        public void println(char[] x) {
            trackContentLength(x);
            trackContentLengthLn();
            this.delegate.println(x);
        }

        public void println(String x) {
            trackContentLength(x);
            trackContentLengthLn();
            this.delegate.println(x);
        }

        public void println(Object x) {
            trackContentLength(x);
            trackContentLengthLn();
            this.delegate.println(x);
        }

        public PrintWriter printf(String format, Object... args) {
            return this.delegate.printf(format, args);
        }

        public PrintWriter printf(Locale l, String format, Object... args) {
            return this.delegate.printf(l, format, args);
        }

        public PrintWriter format(String format, Object... args) {
            return this.delegate.format(format, args);
        }

        public PrintWriter format(Locale l, String format, Object... args) {
            return this.delegate.format(l, format, args);
        }

        public PrintWriter append(CharSequence csq) {
            checkContentLength(csq.length());
            return this.delegate.append(csq);
        }

        public PrintWriter append(CharSequence csq, int start, int end) {
            checkContentLength(end - start);
            return this.delegate.append(csq, start, end);
        }

        public PrintWriter append(char c) {
            trackContentLength();
            return this.delegate.append(c);
        }
    }

    private abstract class SaveContextServletOutputStream extends ServletOutputStream {
        private final ServletOutputStream delegate;

        SaveContextServletOutputStream(ServletOutputStream delegate) {
            this.delegate = delegate;
        }

        public void write(int b) throws IOException {
            trackContentLength(b);
            this.delegate.write(b);
        }

        public void flush() throws IOException {
            doOnResponseCommitted();
            this.delegate.flush();
        }

        public void close() throws IOException {
            doOnResponseCommitted();
            this.delegate.close();
        }

        public int hashCode() {
            return this.delegate.hashCode();
        }

        public boolean equals(Object obj) {
            return this.delegate.equals(obj);
        }

        public void print(boolean b) throws IOException {
            trackContentLength(b);
            this.delegate.print(b);
        }

        public void print(char c) throws IOException {
            trackContentLength();
            this.delegate.print(c);
        }

        public void print(double d) throws IOException {
            trackContentLength(d);
            this.delegate.print(d);
        }

        public void print(float f) throws IOException {
            trackContentLength(f);
            this.delegate.print(f);
        }

        public void print(int i) throws IOException {
            trackContentLength(i);
            this.delegate.print(i);
        }

        public void print(long l) throws IOException {
            trackContentLength(l);
            this.delegate.print(l);
        }

        public void print(String s) throws IOException {
            trackContentLength(s);
            this.delegate.print(s);
        }

        public void println() throws IOException {
            trackContentLengthLn();
            this.delegate.println();
        }

        public void println(boolean b) throws IOException {
            trackContentLength(b);
            trackContentLengthLn();
            this.delegate.println(b);
        }

        public void println(char c) throws IOException {
            trackContentLength();
            trackContentLengthLn();
            this.delegate.println(c);
        }

        public void println(double d) throws IOException {
            trackContentLength(d);
            trackContentLengthLn();
            this.delegate.println(d);
        }

        public void println(float f) throws IOException {
            trackContentLength(f);
            trackContentLengthLn();
            this.delegate.println(f);
        }

        public void println(int i) throws IOException {
            trackContentLength(i);
            trackContentLengthLn();
            this.delegate.println(i);
        }

        public void println(long l) throws IOException {
            trackContentLength(l);
            trackContentLengthLn();
            this.delegate.println(l);
        }

        public void println(String s) throws IOException {
            trackContentLength(s);
            trackContentLengthLn();
            this.delegate.println(s);
        }

        public void write(byte[] b) throws IOException {
            trackContentLength(b);
            this.delegate.write(b);
        }

        public void write(byte[] b, int off, int len) throws IOException {
            checkContentLength(len);
            this.delegate.write(b, off, len);
        }

        public String toString() {
            return getClass().getName() + "[delegate=" + this.delegate.toString() + "]";
        }
    }
}
