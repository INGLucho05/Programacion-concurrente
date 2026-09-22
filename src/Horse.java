import java.util.Random;

// Hilo que representa a un caballo: avanza de forma aleatoria hasta la meta
public class Horse extends Thread {
    private final String name;
    private int progress;
    private final Random random;
    // volatile: garantiza visibilidad de running entre el hilo UI y este hilo
    private volatile boolean running;
    // Meta de la carrera
    private static final int FINISH_LINE = 100;

    public Horse(String name) {
        this.name = name;
        this.progress = 0;
        this.random = new Random();
        this.running = false;
    }

    public String getHorseName() {
        return name;
    }

    public int getProgress() {
        return progress;
    }

    public boolean isFinished() {
        return progress >= FINISH_LINE;
    }

    public void startRunning() {
        this.running = true;
    }

    public void stopRunning() {
        this.running = false;
    }

    // Cuerpo del hilo: avanza mientras este en marcha y no haya llegado a 100
    @Override
    public void run() {
        while (running && progress < FINISH_LINE) {
            // Avance aleatorio entre 1 y 15
            int advance = random.nextInt(15) + 1;
            // Limita el avance para no superar 100
            progress = Math.min(progress + advance, FINISH_LINE);

            try {
                // Pausa corta (100-300 ms) ~ simula el paso del tiempo
                Thread.sleep(100 + random.nextInt(200));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
