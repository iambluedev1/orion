package fr.iambluedev.orion;

import java.util.Scanner;

import lombok.Getter;

public class Main {

	@Getter
	private static Orion instance;

	public static void main(String[] args) {
		instance = Orion.getInstance();
		instance.start();

		Scanner scanner = new Scanner(System.in);

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				instance.stop();
			}
		});

		while (instance.isRunning()) {
			instance.executeCommand(scanner.nextLine());
		}
		
		scanner.close();
		System.exit(0);
	}
}
