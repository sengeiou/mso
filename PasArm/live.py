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
    zs = []
    ds = []
    j = 0
    k = 0
    for line in lines:
        if len(line) > 1:
            if line.find('x') != -1:
                txt, x = line.split('=')
                xs.append(int(x))
            elif line.find('y') != -1:
                txt, y = line.split('=')
                ys.append(int(y))
            elif line.find('z') != -1:
                txt, z = line.split('=')
                zs.append(int(z))
            elif line.find("diff") != -1:
                txt, diff = line.split('=')
                ds.append(int(diff))
            k += 1
            if k == 4:
                j += 1
                k = 0
    ax1.clear()
    ax1.plot(xs, label='x')
    ax1.plot(ys, label='y')
    ax1.plot(zs, label='z')
    ax1.plot(ds, label='diff')
    ax1.set_xlim(left=max(0, j-500), right=j+30)


ani = animation.FuncAnimation(fig, animate, interval=100)
plt.show()
