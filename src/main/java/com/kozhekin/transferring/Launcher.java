package com.kozhekin.transferring;

/**
 * Launcher assembles all parts of an application also initializes and destroys all modules
 */
public class Launcher {

    public static void main(String[] args) {
        ApplicationContext context = new ApplicationContext();
        context.start();
    }
}
