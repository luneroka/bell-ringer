package com.bell_ringer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bellringer.generation")
public class GenerationProperties {

  private int minQuizzesForAdaptive = 3;

  public static class Base {
    private double easy = 0.40;
    private double medium = 0.40;
    private double hard = 0.20;

    // getters/setters
    public double getEasy() { return easy; }
    public void setEasy(double v) { this.easy = v; }
    public double getMedium() { return medium; }
    public void setMedium(double v) { this.medium = v; }
    public double getHard() { return hard; }
    public void setHard(double v) { this.hard = v; }
  }

  private Base base = new Base();
  private double noise = 0.10;
  private double adaptiveAlpha = 0.6;

  // getters/setters
  public int getMinQuizzesForAdaptive() { return minQuizzesForAdaptive; }
  public void setMinQuizzesForAdaptive(int v) { this.minQuizzesForAdaptive = v; }
  public Base getBase() { return base; }
  public void setBase(Base base) { this.base = base; }
  public double getNoise() { return noise; }
  public void setNoise(double v) { this.noise = v; }
  public double getAdaptiveAlpha() { return adaptiveAlpha; }
  public void setAdaptiveAlpha(double v) { this.adaptiveAlpha = v; }
}