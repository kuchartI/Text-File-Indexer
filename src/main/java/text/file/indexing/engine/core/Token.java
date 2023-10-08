package text.file.indexing.engine.core;


public record Token(String token) {
    public static Token fromString(String token) {
        return new Token(token);
    }
    public static Token defaultWhiteSpaceToken(){
        return new Token("\\s+");
    }
}
