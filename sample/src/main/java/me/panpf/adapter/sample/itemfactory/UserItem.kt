package me.panpf.adapter.sample.itemfactory

import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import me.panpf.adapter.AssemblyItem
import me.panpf.adapter.AssemblyItemFactory
import me.panpf.adapter.ktx.bindView
import me.panpf.adapter.sample.R
import me.panpf.adapter.sample.bean.User

class UserItem(itemLayoutId: Int, parent: ViewGroup) : AssemblyItem<User>(itemLayoutId, parent) {
    private val headImageView: ImageView by bindView(R.id.image_userListItem_head)
    private val nameTextView: TextView by bindView(R.id.text_userListItem_name)
    private val sexTextView: TextView by bindView(R.id.text_userListItem_sex)
    private val ageTextView: TextView by bindView(R.id.text_userListItem_age)
    private val jobTextView: TextView by bindView(R.id.text_userListItem_job)

    override fun onSetData(position: Int, user: User?) {
        user ?: return
        headImageView.setImageResource(user.headResId)
        nameTextView.text = user.name
        sexTextView.text = user.sex
        ageTextView.text = user.age
        jobTextView.text = user.job
    }

    class Factory : AssemblyItemFactory<User>() {
        init {
            setOnViewClickListener(R.id.image_userListItem_head) {context, view, position, positionInPart, data ->
                Toast.makeText(context, "别摸我头，讨厌啦！", Toast.LENGTH_SHORT).show()
            }
            setOnViewClickListener(R.id.text_userListItem_name) {context, view, position, positionInPart, data ->
                Toast.makeText(context, "我就叫" + data?.name + "，咋地不服啊！", Toast.LENGTH_SHORT).show()
            }
            setOnViewClickListener(R.id.text_userListItem_sex) {context, view, position, positionInPart, data ->
                Toast.makeText(context, "我还就是" + data?.sex + "个的了，有本事你捅我啊！", Toast.LENGTH_SHORT).show()
            }
            setOnViewClickListener(R.id.text_userListItem_age) {context, view, position, positionInPart, data ->
                val message: String = if ((data?.sex
                                ?: "").contains("男") || (data?.sex ?: "").contains("先生")) {
                    "哥今年" + data?.age + "岁了，该找媳妇了！"
                } else {
                    "姐今年" + data?.age + "岁了，该找人嫁了！"
                }
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
            setOnViewClickListener(R.id.text_userListItem_job) {context, view, position, positionInPart, data ->
                Toast.makeText(context, "我是名光荣的" + data?.job, Toast.LENGTH_SHORT).show()
            }
        }

        override fun match(data: Any?): Boolean {
            return data is User
        }

        override fun createAssemblyItem(parent: ViewGroup): AssemblyItem<User> {
            return UserItem(R.layout.list_item_user, parent)
        }
    }
}
