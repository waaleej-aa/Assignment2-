/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.university.lab.assignment1;

/**
 *
 * @author waleeja
 */
public class digitalwalletmain {
    public static void main(String[] args) {
        DigitalWallet wallet = new DigitalWallet("Waleeja", 1000, "4321");

        System.out.println(wallet.withdraw(200, "4321"));  // correct pin, enough funds
        System.out.println(wallet.getBalance());

        System.out.println(wallet.withdraw(2000, "4321")); // insufficient funds
        System.out.println(wallet.withdraw(100, "0000"));  // wrong pin
    }
}

