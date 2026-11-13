package com.example.a10

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a10.ui.theme._10Theme
import kotlin.random.Random

data class Cart(
    val products: List<Product>
)

data class Product(
    val id: Int,
    val name: String,
    val price: Int
)

/** Сумма заказа, начиная с которой доставка бесплатная. */
private const val FREE_DELIVERY_THRESHOLD = 500

/** Начальное состояние корзины. */
private val StartProducts = listOf(
    Product(0, "Товар #1", 100),
    Product(1, "Товар #2", 150),
    Product(2, "Товар #3", 56)
)


private val CartSaver = listSaver<Cart, Any>(
    save = { cart -> cart.products.flatMap { listOf<Any>(it.id, it.name, it.price) } },
    restore = { saved ->
        Cart(saved.chunked(3).map { Product(it[0] as Int, it[1] as String, it[2] as Int) })
    }
)

class MainActivity : ComponentActivity() {

    private val TAG = "MainActivity" 

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: MainActivity created")
        enableEdgeToEdge()
        setContent {
            _10Theme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ShoppingCartScreen()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart: MainActivity started")
    }

    override fun onRestart() {
        super.onRestart()
        Log.d(TAG, "onRestart: MainActivity restarted")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: MainActivity resumed")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause: MainActivity paused")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop: MainActivity stopped")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: MainActivity destroyed")
    }
}

/**
 * Экран корзины.
 *
 * Единственный источник правды — состояние [cart]. Сумма и количество товаров
 * не хранятся в отдельных переменных, а вычисляются из cart,
 * поэтому рекомпозиция не может «накрутить» лишние деньги.
 */
@Composable
fun ShoppingCartScreen(modifier: Modifier = Modifier) {
    //rememberSaveable — состояние переживает и рекомпозицию, и поворот экрана
    var cart by rememberSaveable(stateSaver = CartSaver) {
        mutableStateOf(Cart(StartProducts))
    }

    val products = cart.products
    val productSize = products.size
    val totalSum = products.sumOf { it.price }

    //Toast — это side effect, поэтому показываем его не в теле композиции,
    //а в LaunchedEffect, который перезапускается только при изменении суммы.
    val context = LocalContext.current
    LaunchedEffect(totalSum) {
        if (totalSum > FREE_DELIVERY_THRESHOLD) {
            Toast.makeText(context, "Доставка бесплатная!", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ProductListSection(products = products)
        Spacer(modifier = Modifier.height(12.dp))
        TotalSumSection(totalSum = totalSum)
        Spacer(modifier = Modifier.height(16.dp))
        AddProductSection(
            onAdd = {
                cart = cart.copy(
                    products = products + Product(
                        id = (products.maxOfOrNull { it.id } ?: -1) + 1,
                        name = "Товар #${products.size + 1}",
                        price = Random.nextInt(50, 200)
                    )
                )
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        RemoveProductSection(
            productSize = productSize,
            onRemove = {
                if (products.isNotEmpty()) {
                    cart = cart.copy(products = products.dropLast(1))
                }
            }
        )
    }
}

@Composable
private fun ProductListSection(products: List<Product>) {
    for (product in products) {
        Text(
            text = product.name + " - " + product.price + " рублей",
            fontSize = 18.sp
        )
    }
}

@Composable
private fun TotalSumSection(totalSum: Int) {
    Text(
        text = "Товаров на сумму: $totalSum рублей",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun AddProductSection(
    onAdd: () -> Unit
) {
    Button(
        onClick = onAdd,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = "Добавить товар")
    }
}

@Composable
private fun RemoveProductSection(
    productSize: Int,
    onRemove: () -> Unit
) {
    //Кнопка видна только при наличии товаров в корзине
    if (productSize > 0) {
        Button(
            onClick = onRemove,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Удалить товар")
        }
    }
}
