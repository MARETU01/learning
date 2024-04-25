import tkinter as tk

class Hotel:
    def __init__(self, name, rooms):
        self.name = name
        self.rooms = rooms

    def display_available_rooms(self):
        print("可用房间：")
        for room_num, room in self.rooms.items():
            if not room['reserved']:
                print(f"房间号：{room_num}, 类型：{room['type']}, 价格：{room['price']}")

    def reserve_room(self, room_num):
        if room_num in self.rooms and not self.rooms[room_num]['reserved']:
            self.rooms[room_num]['reserved'] = True
            print(f"成功预定房间号{room_num}!")
        else:
            print(f"房间号{room_num}不可用或不存在。")

# 创建酒店对象
hotel = Hotel("精品酒店", {
    '101': {'type': '单人间', 'price': 100, 'reserved': False},
    '102': {'type': '双人间', 'price': 150, 'reserved': False},
    '201': {'type': '套房', 'price': 250, 'reserved': False}
})

def display_available_rooms():
    available_rooms = []
    for room_num, room in hotel.rooms.items():
        if not room['reserved']:
            available_rooms.append(f"房间号：{room_num}, 类型：{room['type']}, 价格：{room['price']}")
    room_list.delete(0, tk.END)
    for room in available_rooms:
        room_list.insert(tk.END, room)

def reserve_room():
    selected_index = room_list.curselection()
    if selected_index:
        room_num = room_list.get(selected_index[0]).split('：')[1]
        hotel.reserve_room(room_num)
        display_available_rooms()

# 创建主窗口
window = tk.Tk()
window.title("酒店自助预定系统")

# 欢迎标签
label = tk.Label(window, text="欢迎使用酒店自助预定系统")
label.pack()

# 可用房间列表框
room_list = tk.Listbox(window, width=50)
room_list.pack()

# 刷新按钮
refresh_button = tk.Button(window, text="刷新房间列表", command=display_available_rooms)
refresh_button.pack()

# 预定按钮
reserve_button = tk.Button(window, text="预定房间", command=reserve_room)
reserve_button.pack()

# 显示可用房间列表
display_available_rooms()

# 启动主循环
window.mainloop()