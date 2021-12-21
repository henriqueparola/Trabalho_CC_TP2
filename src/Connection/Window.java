package Connection;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Window {
    public List<byte[]> windowData;
    private List<Boolean>  windowAcked;
    public int base;
    public int nextSeqNum;
    public int N;
    private Lock l = new ReentrantLock();

    public Window(int N) {
        this.N = N;
        this.windowData = new ArrayList<>();

        for(int i = 0; i < N; i++) {
            this.windowData.add(null);
        }

        this.base = 0;
        this.nextSeqNum = 0;
    }

    public void addData(int seq, byte[] packetOut) {
        if (seqInBounds(seq)) {
            int index = seq - base;
            this.windowData.remove(index);
            this.windowData.add(index, packetOut);
            this.nextSeqNum++;
        }

    }

    public void update(int ackNum) {
        for (int i = this.base; i < ackNum; i++) {
            shiftLeft();
        }
    }

    private void shiftLeft() {
        this.windowData.remove(0);
        this.windowData.add(this.N - 1, null);
        this.base++;
    }

    public void receive(int seqNum, byte[] data) {
        l.lock();
        if (data != null && seqInBounds(seqNum)) {
            int index = seqNum - this.base;
            this.windowData.remove(index);
            this.windowData.add(index, data);
        }
        l.unlock();
    }
    // Não esquecer de usar lock para metodos do receive.

    private boolean seqInBounds(int seqNum) {
        return seqNum >= this.base && seqNum < this.base + this.N;
    }

    public byte[] retrieve() {
        byte[] data;
        if ((data = this.windowData.get(0)) != null) {
            //posso remover o primeiro elemento e incrementar base
            data = this.windowData.remove(0);
            this.base++;
            this.windowData.add(null);
        }
        return data;
    }

    public boolean empty() {
        for (byte [] b : this.windowData) {
            if (b != null) return false;
        }
        return true;
    }
}
