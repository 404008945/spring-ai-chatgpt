package org.springframework.ai.openai.samples.helloworld.output;


import java.util.List;

public class AuthorPoems {

    private String author;

    private List<String> titles;


    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public List<String> getTitles() {
        return titles;
    }

    public void setTitles(List<String> titles) {
        this.titles = titles;
    }
}
