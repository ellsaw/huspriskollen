use predictor::k_nearest_neighbors::{k_nearest_neighbors, PropertyType};


// Testing function
fn main() {

    let latitude_test: f64 = 59.636297740796444;
    let longitude_test: f64 = 16.55787719036489;

    let k: usize = 31;

    let ids_properties_in_class: Vec<i32> = match k_nearest_neighbors(PropertyType::Villa, latitude_test, longitude_test, k){
        Ok(id_vector) => id_vector,
        Err(e) => {
            eprintln!("Classification error: {}", e);
            return;
        }
    };

    for id in ids_properties_in_class{
        println!("{}", id);
    }

}
