package com.sinoparasoft.task;

import java.util.Date;
import java.util.Random;

import com.sinoparasoft.model.VirtualMachine;

public class DataThread implements Runnable {
	@Override
	public void run() {
		boolean fire = true;

		while (true == fire) {
			VirtualMachine machine = VirtualMachine.findVirtualMachine("35261246-61a4-48d0-9bbc-a7efd82be10a");

			Random random = new Random();
			int sleepSeconds = random.nextInt(3);
			if (0 >= sleepSeconds) {
				sleepSeconds = 1;
			}

			try {
				Thread.sleep(sleepSeconds * 1000);

				machine.setDeleteTime(new Date());
				machine.lastCommitWinsMerge();
			} catch (InterruptedException e) {
				fire = false;
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}