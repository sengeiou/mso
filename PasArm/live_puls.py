import time
import matplotlib.pyplot as plt
import matplotlib.animation as animation


fig = plt.figure()
ax1 = fig.add_subplot(1, 1, 1)


def animate(i):
    graph_data = open('output.txt', 'r')
    lines = graph_data.read().split('\n')
    graph_data.close()
    xs = []
    ys = []
    j = 0
    k = 0
    for line in lines:
        if len(line) > 1:
            if line.find('mean') != -1:
                txt, x = line.split('=')
                xs.append(int(x))
            elif line.find('raw') != -1:
                txt, y = line.split('=')
                ys.append(int(y))
            k += 1
            if k == 2:
                j += 1
                k = 0
    ax1.clear()
    ax1.plot(xs, label='x')
    ax1.plot(ys, label='y')
    ax1.set_xlim(left=max(0, j-500), right=j+30)


ani = animation.FuncAnimation(fig, animate, interval=100)
plt.show()
